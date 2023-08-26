/**
 * Author:
 * 		Benny Ngo (alone).
 *      Date: 6/19/2022
 *		I developed the algorithm and coding by myself. I did not work with any group.
 *
 * Project: 
 * 		Use the previous project and use a graph to track friend relationships.
 * 		Basically, the project consists of:
 * 
 * A. Project v1
 *		Designing and implementing an application that maintains the data for a simple social network. 
 * 		Each person in the network should have a profile that contains the person’s name, 
 *      current status, and a list of friends. Your application should allow a user to join the network, 
 * 		leave the network, create a profile, modify the profile, search for other profiles, and add friends.
 * B. Enhancement:
 * 		Use a graph to track friend relationships among members of the network.
 * 		Add a feature to enable people to see a list of their friends’ friends.
 * C. Coding:
 *     - Reused SocialNetwork.java
 *     - Implemented UndirectedGraph data member to class Network
 *     - Implemented removeEdge() to DirectedGraph.java and UndirectedGraph.java
 *     - Implemented removeVertex() to DirectedGraph.java
 *     - Implemented disconnect() in Vertex.java
 *     - Modified SocialNetwork.java. 
 *     		- Reworked the menu to add/delete/display one or more friends for a user.
 *          - The Network class maintains a dictionary and a graph. Whenever we add/delete/display, we handle for both
 *            dictionary and graph.
 *     		- In particular, the feature to display a list of their friends’ friends is implemented in Option 11, 
 *     		  which is supported by displayFriendsListsForAllFriendsOfUser(), which calls the graph method getNeighborIterator().
 * D. Summary:
 * 	  I will try to answer the questions asked in this project
 * 1. What data structures did you use in your code and why?
 * 		  a. class Profile to store user name, status, and a friends list of the user.
 * 		  b. private AList<String> friendsList;     // to maintain the list of friends in a user's profile.
 *	  	  c. private HashedDictionary<String, Profile> users; // to maintain a database of all users' profiles.
 *	  d. private UndirectedGraph<String> graph; // to maintain friendship between a user and other users based on user names.
 *     I used UndirectedGraph because the friendship is bilateral.
 * 2. You should show me in your code where you used these data structures and where you created objects of their classes.
 * 	  	  - Structures (a), (c), (d), are in class Network in SocialNetwork.java.
 * 	  	    I created object (a) whenever we need to create a new user to join a network; it is in menu option 1.   
 * 	  	  - Structure (b) in AList.java
 *	  	    I used object (b) when we add one or more friends to a user. It is also updated when we delete a friend.
 * 	  	  - I created objects (c), (d) in the constructor of Network().
 * 	  	    I used object (c) and (d) when we add/show/delete any users or friends from the network.
 */
package foo;

import java.util.Scanner;
import java.util.Iterator;

enum Status {
	Offline, Online, Busy
}

class Profile {
    private String name;
    private Status status;
    private AList<String> friendsList;
    
    public Profile() {
        name = "";
        status = Status.Offline;
        friendsList = new AList<String>();
    }
    
    public String getName() {
    	return name;
    }
    
    public void setName(String newName) {
    	name = newName;
    }
    
    public Status getStatus() {
    	return status;
    }
    
    public void setStatus(Status stat) {
    	status = stat;
    }
    
    public AList<String> getFriends()
    {
        return friendsList;
    }
    
    public void setFriends(AList<String> list) {
    	friendsList = list;
    }

    public void addFriend(String friendName) {
        if (!friendsList.contains(friendName)) {
            friendsList.add(friendName);
        }
    }

    public void removeFriend(String friendName) {
    	for (int i = 1; i <= friendsList.getLength(); i++) {
    		if (friendsList.getEntry(i).equals(friendName)) {
    			//System.out.println("Remove friend \"" + friendsList.getEntry(i) + "\" from user \"" + name + "\"");
    			friendsList.remove(i);    			
    			break;
    		}
    	}
    }
    
    public void displayFriendsList() {
        if (friendsList.getLength() == 0) {
        	System.out.println("   Friends: No friends");
        }
        else {
        	System.out.println("   Friends:");
        	for (int i = 1; i <= friendsList.getLength(); i++) {
        		if (i > 1) {
        			System.out.print(",");
        		}
        		System.out.print(" " + friendsList.getEntry(i));
        		if (i % 20 == 0) {
        			System.out.println("      ");
        		}
        	}
        	System.out.println();
        }
    }
        
    public void displayStatus() {
    	System.out.println("   Name: " + name + "\n   Status: " + status);
    }
    
    public void display() {
    	displayStatus();
    	displayFriendsList();    
    }
    
    public void create() {
    	readUserName();
		// Default status is Online.
		// This is because when a user requests to be added to the network, he/she must be online	
		status = Status.Online; 
    }   
    
    public void deleteAllFriends() {
    	friendsList.clear();
    }
    
    public void readUserName() {
    	Scanner input = new Scanner(System.in);
    	System.out.print("Enter username: ");
    	name = input.nextLine();
    }
    
    public void readStatus() {
    	boolean invalid = true;
    	Scanner input = new Scanner(System.in);
    	while (invalid) {
    		System.out.print("Enter status (online, offline, busy): ");
	    	String stat = input.nextLine();
	    	invalid = false;
	    	if (stat.equals("online")) {
	    		status = Status.Online;	
	    	}
	    	else if (stat.equals("offline")) {
	    		status = Status.Offline;
	    	}
	    	else if (stat.equals("busy")) {
	    		status = Status.Busy;
	    	}
	    	else {
	    		System.out.println("Invalid. Try again: ");
	    		invalid = true;
	    	}
    	}
    }
    
    public String readFriendName() {
    	Scanner input = new Scanner(System.in);
    	System.out.print("Enter friend name: ");
    	return input.nextLine();
    }	
}

class Network {
	private HashedDictionary<String, Profile> users;
	private UndirectedGraph<String> graph;
	
	public Network() {
		users = new HashedDictionary<String, Profile>();
		graph = new UndirectedGraph<String>();
	}
	
	public UndirectedGraph<String> getGraph() {
		return graph;
	}
	
	public HashedDictionary<String, Profile> getUsers() {
		return users;
	}

    public void create(Profile user) {
    	String userName = user.getName();
        users.add(userName, user);
        graph.addVertex(userName);
    }
    
    public Profile read(String userName) {
      return users.getValue(userName);
    }
    
    public String readExistingUserName() {
    	Scanner input = new Scanner(System.in);
    	while (true) {
	    	System.out.print("Enter username: ");
	    	String userName = input.nextLine();
	    	Profile prof = read(userName);
	    	if (prof == null) {
	    		System.out.println("User " + userName + " is not in the network yet. Try again.");
	    		continue;
	    	}
	    	return userName;
    	}
    }
    
    public String readExistingFriendName() {
    	Scanner input = new Scanner(System.in);
    	while (true) {
	    	System.out.print("Enter friend name: ");
	    	String friendName = input.nextLine();
	    	Profile prof = read(friendName);
	    	if (prof == null) {
	    		System.out.println("User " + friendName + " is not in the network yet. Try again.");
	    		continue;
	    	}
	    	return friendName;
    	}
    }
    
    public AList<String> readExistingFriends(String userName) {
    	AList<String> friendsList = new AList<String>();
    	Scanner input = new Scanner(System.in);
    	System.out.println("Enter friend names, one name per line, or empty line to end:");
    	while (true) {
        	String friendName = input.nextLine();
        	if(friendName.isEmpty()) {
        		break;
        	}
        	if (getProfile(friendName) == null) {
        		System.out.println("Friend's name " + friendName + " is not in the network yet.");
        		System.out.println("Try another friend name or enter empty line to end:");
        		continue;
        	}
        	// Add friend only if he/she is in the network.
        	friendsList.add(friendName);
    	}
    	return friendsList;
    }
    
    public void addFriendship(String userName, String friendName) {
    	// Friendship is bi-directional
    	if (userName.equals(friendName)) {
    		System.out.println("Ignore friend's name \"" + userName + "\" because it is the same user.");
    		return;
    	}
    	Profile user = read(userName);
    	AList<String> friends = user.getFriends();
    	if (friends.contains(friendName)) {
    		System.out.println("Users \"" + userName + "\" and \"" + friendName + "\" are already friends.");
    		return;
    	}
    	
    	user.getFriends().add(friendName);
    	Profile friend = read(friendName);
    	friend.getFriends().add(userName);
    	if (graph.addEdge(userName, friendName, 1)) {
    		System.out.println("Users \"" + userName + "\" and \"" + friendName + "\" are now friends.");
    	}
    }
    
    public void addOneOrMoreFriends(String userName) {
    	AList<String> friends = readExistingFriends(userName);
        if (friends.isEmpty()) {
    		System.out.println("User \"n" + userName + "\" has no friends to add.");
        } else {
        	Profile prof = getProfile(userName);
        	for (int i = 1; i <= friends.getLength(); i++) {
        		String friendName = friends.getEntry(i);    
        		addFriendship(userName, friendName);
        	}
        }
    }
    
    /**
     * @param userName
     * @param friendName
     * The method removes the friendships between 2 users userName and friendName, i.e., removing
     * their names from the friends lists of each user. So the lengths of their friends lists are shrunk by one.
     */
    private void removeFriendship(String userName, String friendName) {
    	// Friendship is a two-way relationship
    	
    	Profile user = read(userName);
    	user.removeFriend(friendName); // this shrinks the friends list of userName by 1 entry

    	Profile friend = read(friendName);
    	friend.removeFriend(userName); // this shrinks the friends list of friendName by 1 entry
    	
    	if (graph.removeEdge(userName, friendName)) {
    		System.out.println("Users \"" + userName + "\" and \"" + friendName + "\" are no longer friends.");
    	}
    }

    public void removeOneOrMoreFriends(String userName) {
    	AList<String> friendsToRemove = readExistingFriends(userName); // this list is not from the profile.
    	for (int i = 1; i <= friendsToRemove.getLength(); i++) {
    		// The value remains fixed in subsequent iterations		
    		if (friendsToRemove.getEntry(i) != null) {
    			removeFriendship(userName, friendsToRemove.getEntry(i));
    			// The method removeFriendship removed only entries from the lists on the profiles.
    			// Thus, all entries in friendsToRemove list are intact, and we can safely walk from
    			// i = 1 to friendsToRemove.getLength() and access .getEntry(i).
    		}
    	}
    }
        
    public int removeAllFriends(String userName) {
    	Profile prof = getProfile(userName);
    	AList<String> friends = prof.getFriends();
    	int count = friends.getLength();
    	
		// We cannot use the for-loop as shown in removeOneOrMoreFriends() 
    	//     	for (int i = 1; i <= friends.getLength(); i++) 
    	// because i marches from 1 through the original length, but the list has fewer entries
    	// after each iteration. Thus, sooner or later, i will exceed the remaining range of the list.
    	while (friends.getLength() > 0) {
    		String friendName = friends.getEntry(1);  
    		removeFriendship(userName, friendName);
    	}
    	return count;
    	
    }

    public void delete(String userName) {
    	if (users.contains(userName)) {    	
    		removeAllFriends(userName);
        	users.remove(userName);
        	// Finally, remove from graph
    		if (graph.removeVertex(userName)) {
    			System.out.println("User \"" + userName + "\" is deleted from the network");
    		}
    	}
    	else {
    		System.out.println("User \"" + userName + "\" is not found in the network");
    	}
    }
    
    public void display(Profile user) {
    	user.display();
    }
    
    public void displaySubGraph(String userName) {
		System.out.println("Users = " + userName);
		QueueInterface<String> allUsers = graph.getBreadthFirstTraversal(userName);
		int totalUsers = 0;
		while (!allUsers.isEmpty()) {
			String name = allUsers.dequeue();
			System.out.println("User " + name);
			totalUsers++;
		}
		if (totalUsers == 0) {
    		System.out.println("Network has no users.");
		}
    }
    
    public void displayAllUsersByDictionary() {
    	if (users.isEmpty()) {
    		System.out.println("Network has no users.");
    	}
    	else {
    		users.displayValidEntries();
    	}
    }
    
    public void joinNetwork(Profile userProf) {
    	create(userProf);
    }
    
    public void leaveNetwork(String userName) {
    	delete(userName);
    }
    
    public Profile getProfile(String userName) {
    	return read(userName);
    }
    
    public void modifyProfile(String userName, Status status) {
    	Profile user = read(userName);
    	user.setStatus(status);
    }
    
    public void display(String userName) {
    	read(userName).display();
    }
    
    public void displayFriendsListOfEveryUser() {

    	Iterator<String> userNames = users.getKeyIterator();
    	int totalUsers = 0;
    	while (userNames.hasNext()) {
    		String userName = userNames.next();
    		displayFriendsListForUser(userName);
        	totalUsers++;
    	}
    	if (totalUsers == 0) {
    		System.out.println("Network has no users");
    		return;
    	}
    }
    
    private LinkedQueue<String> getFriendNamesFromGraph(String userName) {
    	VertexInterface<String> userNameVertex = graph.getVertices().getValue(userName);
    	Iterator<VertexInterface<String>> neighbors = userNameVertex.getNeighborIterator();
    	LinkedQueue<String> listOfNames = new LinkedQueue<String>();
    	while (neighbors.hasNext())
		{
			VertexInterface<String> nextNeighbor = neighbors.next();
			listOfNames.enqueue(nextNeighbor.getLabel());
		}
    	return listOfNames;
    }
    
    public int displayFriendsListForUser(String userName) {
    	LinkedQueue<String> friendNames = getFriendNamesFromGraph(userName);
		int friendscount = 0;
    	while (!friendNames.isEmpty()) {
    		String name = friendNames.dequeue();
    		if (++friendscount == 1) {     			
	    		System.out.print("Friends of " + userName + ": " + name);
    		} else if (friendscount == 20) {
    			System.out.print(",\n\t" + name);
    		} else {
    			System.out.print(", " + name);
    		}
    	}
    	if (friendscount == 0) {
    		System.out.print("User \"" + userName + "\" has no friends.");
    	}
    	System.out.println();
    	return friendscount;
    }
    
    public void displayFriendsListsForAllFriendsOfUser(String userName) {
    	int friendscount = displayFriendsListForUser(userName);
    	if (friendscount == 0) {
    		return;
    	}
    	LinkedQueue<String> friendNames = getFriendNamesFromGraph(userName);
    	while (!friendNames.isEmpty()) {
    		String friendName = friendNames.dequeue();
    		System.out.print("   - ");
    		displayFriendsListForUser(friendName);
    	}
    }
}

public class SocialNetwork {
    public static void main(String[] args) {
    	System.out.println("*** Welcome to Social Network CIS22C ***\n");
    	
    	Network network = new Network();
        
    	String option;
        int optionNum = 0;
        final int OPTION_QUIT = 13;
        
        while (optionNum != OPTION_QUIT) {
	        System.out.println("\nOptions:");
	        System.out.println("    1: Create a user profile to join the network");
	        System.out.println("    2: Add one or more friends to a profile");
	        System.out.println("    3: Delete one or more friends from a profile");
	        System.out.println("    4: Delete all friends from a profile");
	        System.out.println("    5: Modify the status of a profile");
	        System.out.println("    6: Leave network (Delete a profile)");
	        System.out.println("    7: Display all users of the network");
	        
	        System.out.println("    8: Display full profile of a user");
	        System.out.println("    9: Display status of a user");
	        System.out.println("   10: Display a friends list of a user");
	        System.out.println("   11: Display friends lists of all friends of a user");
	        System.out.println("   12: Display friends lists of all users");	        
	        System.out.println("   13: Quit");
	        
            System.out.print("Enter your option: ");
            {
            	Scanner input = new Scanner(System.in);
            	option = input.nextLine();
            }
            
            try {
            	optionNum = Integer.parseInt(option);
            }
            catch(Exception e) {
            	System.out.println("Invalid option: " + e);
            	optionNum = 0; // set to an invalid value so that the retry code below will be executed
            }
            
            if (optionNum < 1 && optionNum > OPTION_QUIT) {
            	System.out.println("Invalid option, try again");
            	continue;
            }
        	Profile prof = new Profile();
        	Status status;
            String userName;
            
            switch(optionNum) {
            case 1:
            	prof.create();
            	System.out.println("*** Join new user to the network ***");
            	network.joinNetwork(prof);
            	prof.display();
            	break;
            case 2:
            	userName = network.readExistingUserName();
            	System.out.println("*** Add one or more friends to user " + userName + " ***");
            	network.addOneOrMoreFriends(userName);
            	break;
            case 3: 
            	userName = network.readExistingUserName();
            	System.out.println("*** Delete one or more friends from user " + userName + " ***");
            	network.removeOneOrMoreFriends(userName);
            	break;            	
            case 4:
            	userName = network.readExistingUserName();
            	System.out.println("*** Delete all friends from user " + userName + " ***");
            	network.removeAllFriends(userName);
            	break;      	
            case 5: 
            	userName = readUserName();
            	status = readStatus();
            	System.out.println("*** Modify user status ***");
            	prof = network.read(userName);
            	prof.setStatus(status);
            	network.getProfile(userName).displayStatus();
            	break; 
            case 6:
            	userName = readUserName();
            	System.out.println("*** Delete user " + userName + " from the network ***");
            	network.delete(userName);
            	break;
            case 7:
            	System.out.println("*** Display all users in the network ***");
            	network.displayAllUsersByDictionary();
            	break; 	
            case 8: 
            	userName = readUserName();
            	System.out.println("*** Display user profile ***");
            	network.getProfile(userName).display();
            	break;            	
            case 9: 
            	userName = readUserName();
            	System.out.println("*** Display user status ***");
            	network.getProfile(userName).displayStatus();
            	break;            	
            case 10: 
            	userName = readUserName();
            	System.out.println("*** Display all friends of user " + userName + " ***");            	
            	network.displayFriendsListForUser(userName);
            	break;
            case 11:
            	userName = network.readExistingUserName();
            	System.out.println("*** Display the friends lists of all friends of user " + userName + " ***");
            	network.displayFriendsListsForAllFriendsOfUser(userName);
            	break;            	
            case 12:
            	System.out.println("*** Display the friends lists of all users in the network ***");
            	network.displayFriendsListOfEveryUser();
            	break;
            case 13:
            	System.out.println("Bye");
            	break;
            default:
            	break;
            }
        }
    }
    
    public static String readUserName() {
    	Scanner input = new Scanner(System.in);
    	System.out.print("Enter username: ");
    	String name = input.nextLine();
    	return name;
    }

    public static Status readStatus() {
    	Status status = Status.Offline;
    	boolean invalid = true;
    	Scanner input = new Scanner(System.in);
    	while (invalid) {
    		System.out.print("Enter status (online, offline, busy): ");
	    	String stat = input.nextLine();
	    	invalid = false;
	    	if (stat.equals("online")) {
	    		status = Status.Online;	
	    	}
	    	else if (stat.equals("offline")) {
	    		status = Status.Offline;
	    	}
	    	else if (stat.equals("busy")) {
	    		status = Status.Busy;
	    	}
	    	else {
	    		System.out.println("Invalid. Try again: ");
	    		invalid = true;
	    	}
    	}
    	return status;
    }
}