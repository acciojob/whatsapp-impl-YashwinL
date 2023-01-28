package com.driver;

import java.text.SimpleDateFormat;
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
        this.usersdb = new HashMap<>();
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

        messageId++;

        Message message = new Message(messageId,content,new Date());

        messagedb.put(messageId,message);
        return messageId;
    }


    public int sendMessage(Message message, User sender, Group group) throws Exception{

        if(!groupUserMap.containsKey(group)){
            throw new Exception("Group does not exist");
        }
        if(!isUserAlreadyExisted(group,sender)){
            throw new Exception("You are not allowed to send message");
        }
        List<Message> lis =new ArrayList<>();
        if(groupMessageMap.containsKey(group)){
            lis = groupMessageMap.get(group);
        }
        lis.add(message);
        groupMessageMap.put(group,lis);
        return lis.size();
    }

    public String changeAdmin(User approver, User user, Group group) throws Exception{
        if(!groupUserMap.containsKey(group)) throw new Exception("Group does not exist");
        if(!adminMap.get(group).equals(approver)) throw new Exception("Approver does not have rights");
        if(!this.isUserAlreadyExisted(group, user)) throw  new Exception("User is not a participant");

        adminMap.put(group, user);
        return "SUCCESS";
//
    }

    public boolean isUserAlreadyExisted(Group group,User user){
        List<User> lis = groupUserMap.get(group);
        for (User li : lis) {
            if (Objects.equals(li.getName(), user.getName())) {
                return true;
            }
        }
        return false;
    }
}
