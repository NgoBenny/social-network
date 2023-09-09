import pandas as pd
import json
import socket
import sqlite3
import matplotlib.pyplot as plt
import tkinter as tk


class UserInterface:
    def __init__(self, select_callback):
        self.select_callback = select_callback
        self.root = tk.Tk()
        self.root.title("Select a state")

        self.state_var = tk.StringVar()
        self.state_var.set("State Options")

        state_options = ["Alabama", "Alaska", "Arizona", "Arkansas", "California", "Colorado",
                         "Connecticut", "Delaware", "Florida", "Georgia", "Hawaii", "Idaho",
                         "Illinois", "Indiana", "Iowa", "Kansas", "Kentucky", "Louisiana",
                         "Maine", "Maryland", "Massachusetts", "Michigan", "Minnesota",
                         "Mississippi", "Missouri", "Montana", "Nebraska", "Nevada",
                         "New Hampshire", "New Jersey", "New Mexico", "New York",
                         "North Carolina", "North Dakota", "Ohio", "Oklahoma", "Oregon",
                         "Pennsylvania", "Rhode Island", "South Carolina", "South Dakota",
                         "Tennessee", "Texas", "Utah", "Vermont", "Virginia", "Washington",
                         "West Virginia", "Wisconsin", "Wyoming"]

        state_menu = tk.OptionMenu(self.root, self.state_var, *state_options)
        state_menu.pack(pady=10)

        select_button = tk.Button(self.root, text="Select", command=self.select_state)
        select_button.pack(pady=10)

        self.root.mainloop()

    def select_state(self):
        self.select_callback(self.state_var.get())


class BusinessLayer:
    def __init__(self):
        self.db = sqlite3.connect("USAStatesCO2.db")

    def process_query(self, states):
        # Construct SQL query
        query = f"SELECT * FROM co2_data WHERE state='{states}' AND year BETWEEN 1970 AND 2020"

        # Execute query and fetch data
        cursor = self.db.execute(query)
        data = cursor.fetchall()

        if not data:
            return None

        # Convert data to JSON string
        data_dict = {"data": []}
        for row in data:
            state = row[0]
            year = row[1]
            co2 = row[2]
            data_dict["data"].append({"year": year, "co2": co2, "state": state})

        # convert data_dict to JSON format
        json_data = json.dumps(data_dict)

        # Debugging purposes
        print(data_dict)

        return json_data


class DataLayer:
    def __init__(self, db_file):
        self.db_file = db_file
        self.conn = sqlite3.connect(self.db_file)
        self.conn.row_factory = sqlite3.Row

    def create_table(self):
        self.conn.execute("CREATE TABLE IF NOT EXISTS co2_data (year INT, co2 REAL, state TEXT)")

        df = pd.read_csv('USAStatesCO2.csv', skiprows=4)
        header = df.columns.tolist()
        df = df.drop(columns=['Percent', 'Absolute'])
        df = df.melt(id_vars=['State'], var_name='Year', value_name='CO2')

        df.to_sql('co2_data', self.conn, if_exists='replace', index=False)

    def query(self, state=None, year=None):
        query = "SELECT year, co2, state FROM co2_data"
        params = []

        if state is not None:
            query += " WHERE state = ?"
            params.append(state)

        if year is not None:
            if not params:
                query += " WHERE"
            else:
                query += " AND"
            query += " year = ?"
            params.append(year)

        cur = self.conn.cursor()
        cur.execute(query, params)
        rows = cur.fetchall()

        results = []
        for row in rows:
            results.append(dict(row))

        return json.dumps(results)


class GraphicLayer:
    def __init__(self):
        pass

    def plot_data(self, data_dict, state):
        years = [int(d["year"]) for d in data_dict["data"]]
        co2 = [float(d["co2"]) for d in data_dict["data"]]

        plt.plot(years, co2)
        plt.title(f"CO2 Emissions in {state}")
        plt.xlabel("Year")
        plt.ylabel("CO2 Emissions (metric tons per capita)")
        plt.show()


class ServerSocket:
    def __init__(self, host, port, data_layer):
        self.host = host
        self.port = port
        self.data_layer = data_layer

    def start(self):
        with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
            s.bind((self.host, self.port))
            s.listen()
            while True:
                conn, addr = s.accept()
                with conn:
                    data = conn.recv(1024)
                    query_string = f"SELECT year, co2, state FROM co2_data WHERE state = '{data.decode()}'"
                    result = self.data_layer.query(query_string)
                    json_data = json.dumps(result)
                    conn.sendall(result.encode())


class ClientSocket:
    def __init__(self, host="localhost", port=5555):
        self.host = host
        self.port = port

    def request_data(self, state):
        with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
            s.connect((self.host, self.port))
            message = {"state": state}
            s.sendall(json.dumps(message).encode())
            data = s.recv(1024)
            return json.loads(data.decode())


def main():
    data_layer = DataLayer("USAStatesCO2.db")
    data_layer.create_table()
    business_layer = BusinessLayer()
    graphic_layer = GraphicLayer()
    server_socket = ServerSocket("localhost", 5555, data_layer)
    client_socket = ClientSocket()

    # User interface
    def select_callback(state):
        data = json.loads(business_layer.process_query(state))
        graphic_layer.plot_data(data, state)

    user_interface = UserInterface(select_callback)

    # Server socket
    server_socket.start()

    # Client socket
    state = user_interface.state_var.get()
    data = client_socket.request_data(state)
    graphic_layer.plot_data(data, state)


if __name__ == '__main__':
    main()
