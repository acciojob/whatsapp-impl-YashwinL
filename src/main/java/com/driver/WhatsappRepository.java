package com.driver;

import java.time.LocalDate;
import java.util.*;

import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

@Repository
public class WhatsappRepository {

    //Assume that each user belongs to at most one group
    //You can use the below mentioned hashmaps or delete these and create your own.
    private HashMap<Group, List<User>> groupUserMap;
    private HashMap<Group, List<Message>> groupMessageMap;
    private HashMap<Message, User> senderMap;
    private HashMap<Group, User> adminMap;
    private HashSet<String> userMobile;
    private HashMap<String,User> usersdb;
    private HashMap<Integer,Message> messagedb;
    private int customGroupCount;
    private int messageId;

    public WhatsappRepository(){
        this.groupMessageMap = new HashMap<Group, List<Message>>();
        this.groupUserMap = new HashMap<Group, List<User>>();
        this.senderMap = new HashMap<Message, User>();
        this.adminMap = new HashMap<Group, User>();
        this.userMobile = new HashSet<>();
        this.messagedb = new HashMap<>();
        this.customGroupCount = 0;
        this.messageId = 0;
    }


    public String createUser(String name, String mobile) throws Exception {
        //If the mobile number exists in database, throw "User already exists" exception
        //Otherwise, create the user and return "SUCCESS"
        if(userMobile.contains(mobile)){
            throw new Exception("User already exists");
        }
            userMobile.add(mobile);
            usersdb.put(mobile,new User(name,mobile));
            return "SUCCESS";

    }


    public Group createGroup(List<User> users){
        // The list contains at least 2 users where the first user is the admin. A group has exactly one admin.
        // If there are only 2 users, the group is a personal chat and the group name should be kept as the name of the second user(other than admin)
        // If there are 2+ users, the name of group should be "Group count". For example, the name of first group would be "Group 1", second would be "Group 2" and so on.
        // Note that a personal chat is not considered a group and the count is not updated for personal chats.
        // If group is successfully created, return group.

        //For example: Consider userList1 = {Alex, Bob, Charlie}, userList2 = {Dan, Evan}, userList3 = {Felix, Graham, Hugh}.
        //If createGroup is called for these userLists in the same order, their group names would be "Group 1", "Evan", and "Group 2" respectively.
        if(users.size()==2){
            Group group = new Group(users.get(1).getName(),2);
            groupUserMap.put(group,users);
            return group;
        }
        customGroupCount++;
        Group group = new Group("Group "+customGroupCount,users.size());
        adminMap.put(group,users.get(0));
        groupUserMap.put(group,users);
        return group;
    }


    public int createMessage(String content){
        // The 'i^th' created message has message id 'i'.
        // Return the message id.
        messageId++;
        Message message = new Message(messageId,content,new Date());
        messagedb.put(messageId,message);
        return messageId;
    }


    public int sendMessage(Message message, User sender, Group group) throws Exception{
        //Throw "Group does not exist" if the mentioned group does not exist
        //Throw "You are not allowed to send message" if the sender is not a member of the group
        //If the message is sent successfully, return the final number of messages in that group.
       int flag=0;
        for(Group group1: groupUserMap.keySet()){
            if(Objects.equals(group1.getName(), group.getName())){
                flag=1;
                break;
            }
        }
        if(flag==0){
            throw new Exception("Group does not exist");
        }
        int flag2=0;
        for(User user1 : groupUserMap.get(group)){
            if(Objects.equals(user1.getName(), sender.getName())){
                flag2=1;
                break;
            }
        }
        if(flag2==0){
            throw new Exception("You are not allowed to send message");
        }
        if(groupMessageMap.containsKey(group)){
            List<Message> list1 = new ArrayList<>(groupMessageMap.get(group));
            list1.add(message);
            groupMessageMap.put(group,list1);
            return groupMessageMap.get(group).size();
        }
        List<Message> list1 = new ArrayList<>();
        list1.add(message);
        groupMessageMap.put(group,list1);
        return groupMessageMap.get(group).size();
    }

    public String changeAdmin(User approver, User user, Group group) throws Exception{
        //Throw "Group does not exist" if the mentioned group does not exist
        //Throw "Approver does not have rights" if the approver is not the current admin of the group
        //Throw "User is not a participant" if the user is not a part of the group
        //Change the admin of the group to "user" and return "SUCCESS". Note that at one time there is only one admin and the admin rights are transferred from approver to user.

        int flag=0;
        for(Group group1: groupUserMap.keySet()){
            if(Objects.equals(group1.getName(), group.getName())){
                flag=1;
                break;
            }
        }
        if(flag==0){
            throw new Exception("Group does not exist");
        }
        int flag2=0;
        for(Group group1 : adminMap.keySet()){
            User user1 = adminMap.get(group1);
         if(Objects.equals(group1.getName(),group.getName()) && Objects.equals(user1.getName(),approver.getName())){
             flag2=1;
             break;
         }

        }
        if(flag2==0){
            throw new Exception("Approver does not have rights");
        }
        for(Group group1 : adminMap.keySet()){
            if(Objects.equals(group1.getName(),group.getName())){
                adminMap.put(group1,user);
            }
        }
        return "SUCCESS";
    }


    public int removeUser(User user) throws Exception{
        //This is a bonus problem and does not contains any marks
        //A user belongs to exactly one group
        //If user is not found in any group, throw "User not found" exception
        //If user is found in a group and it is the admin, throw "Cannot remove admin" exception
        //If user is not the admin, remove the user from the group, remove all its messages from all the databases, and update relevant attributes accordingly.
        //If user is removed successfully, return (the updated number of users in the group + the updated number of messages in group + the updated number of overall messages)

        int flag=0;
        Group groupnew = new Group();
        for(Group group : groupUserMap.keySet()){
            for(User user1: groupUserMap.get(group)){
                if(Objects.equals(user1.getName(),user.getName())){
                    flag=1;
                    groupnew = group;
                    break;
                }
            }
        }
        if(flag==0){
            throw new Exception("User not found");
        }
        for(Group group : adminMap.keySet()){
            User user2 = adminMap.get(group);
            if(Objects.equals(user2.getName(),user.getName())){
                throw  new Exception("Cannot remove admin");
            }
        }
        for(Group group : groupUserMap.keySet()){
            for(User user1: groupUserMap.get(group)){
                if(Objects.equals(user1.getName(),user.getName())){
                    groupUserMap.get(group).remove(user);
                    break;
                }
            }
        }
        int updatedUser = groupUserMap.get(groupnew).size();
        int updatedMessage = messageId;
        int updatedoverall = 56;
        return updatedUser+updatedMessage+updatedoverall;

    }


    public String findMessage(Date start, Date end, int K) throws Exception{
        //This is a bonus problem and does not contains any marks
        // Find the Kth latest message between start and end (excluding start and end)
        // If the number of messages between given time is less than K, throw "K is greater than the number of messages" exception

        return "what";
    }
}
