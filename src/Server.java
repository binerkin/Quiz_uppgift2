import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server {

    private static final int PORT = 5674;
    static String correctAnswer=" ";
    static String questionToSend = " ";
    static boolean questionIsAnswered =false;
    static ArrayList<User> users = new ArrayList<>();
    private static HashSet<String> names = new HashSet<String>();
    private static HashSet<PrintWriter> writers = new HashSet<PrintWriter>();

    public static void main(String[] args) throws Exception {
        System.out.println("The chat server is running.");
        new Thread(new QuestionThread()).start();
        ServerSocket listener = new ServerSocket(PORT);
        try {
            while (true) {
                new Handler(listener.accept()).start();
            }
        } finally {
            listener.close();
        }
    }


    private static class Handler extends Thread {
        private String name;
        private Socket socket;
        static BufferedReader in;
        static PrintWriter out;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(
                        socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                //Check if username is in use
                while (true) {
                    out.println("SUBMITNAME");
                    System.out.println("Requesting username from new user");
                    name = in.readLine();
                    if (name == null) {
                        return;
                    }
                    synchronized (names) {
                        if (!names.contains(name)) {
                            names.add(name);
                            System.out.println(name+" has joined");
                            break;
                        }
                    }
                }

                out.println("NAMEACCEPTED");
                User theNewUser= new User();
                writers.add(out);
                theNewUser.addUser(String.valueOf(name),0, String.valueOf(socket.getInetAddress().getHostAddress()));

                while (true) {
                    boolean violation=false;

                    //Check if the correct answer is printed
                    String input = in.readLine();
                    System.out.println("input is: "+input);
                    System.out.println("Correct answer is: "+correctAnswer);
                    if (input == null) {
                        return;
                    } else if (input.toLowerCase().equals(correctAnswer)) {
                        if(questionIsAnswered == false){
                            System.out.println("Correct answer has been given");
                            for (PrintWriter writer : writers) {
                                writer.println("CORRECTANSWER " + correctAnswer + " is correct!");
                                questionIsAnswered=true;
                            }

                            //Adds score to the user
                            User newScore = new User();
                            newScore.addScore(String.valueOf(socket.getInetAddress().getHostAddress()));
                            newScore.printUsers();
                        }else{
                            for (PrintWriter writer : writers) {
                                writer.println("MESSAGE " + "A correct answer is already given...");
                            }
                        }

                    }else if (input.toLowerCase().equals("/quit")){
                        try {
                            //Remove user if printing /quit
                            socket.close();
                            User removeUser = new User();
                            removeUser.removeUser(String.valueOf(socket.getInetAddress().getHostAddress()));
                        } catch (IOException e) {
                        }
                        //Violation checker
                    }else if(input.toLowerCase().contains("shit")||input.toLowerCase().contains("damn")||input.toLowerCase().contains("sucker")) {
                        for (PrintWriter writer : writers) {
                            writer.println("MESSAGE INAPPROPRIATE WORDS ARE NOT ACCEPTED");
                            violation=true;
                        }
                    }
                    //Placeholder for further additions
                    else if(!violation){
                        for (PrintWriter writer : writers) {
                            writer.println("MESSAGE " + name + ": " + input);
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println(e);
            } finally {
                if (name != null) {
                    names.remove(name);
                }
                if (out != null) {
                    writers.remove(out);
                }
                try {
                    socket.close();
                    User removeUser = new User();
                    removeUser.removeUser(String.valueOf(socket.getInetAddress().getHostAddress()));
                } catch (IOException e) {
                }
            }
        }
    }

    //QUESTION THREAD
    //Creates a new question
    static class QuestionThread extends Thread {
        Timer timer = new Timer();

        @Override
        public void run() {

            System.out.println("Question generator initiated");

            TimerTask myTask = new TimerTask() {
                @Override
                public void run() {
                    Question theQuestion = new Question();
                    theQuestion.newQuestion();
                    System.out.println("New question created");
                    questionIsAnswered=false;
                }
            };

            timer.schedule(myTask, 0, 15000);
        }
    }

    //QUESTION CLASS
    //Randomises questions and checks if the answers match the correct answer.
    private static class Question extends Server {

        private void question(){
            Random ran = new Random();
            int randomNumber = ran.nextInt(6)+1;
            switch (randomNumber) {
                case 1: questionToSend = "In what country can you find the city of Petra?"; correctAnswer = "jordan";
                    break;
                case 2:  questionToSend = "In what capital can you find near the pyramids of Giza?"; correctAnswer = "cairo";
                    break;
                case 3:  questionToSend = "Who build Machu picchu?"; correctAnswer = "mayans";
                    break;
                case 4:  questionToSend = "Which egyptian pharaoh was buried in the great pyramid of Giza?"; correctAnswer = "khufu";
                    break;
                case 5:  questionToSend = "In what city can you find Cristo redentor?"; correctAnswer = "rio";
                    break;
                case 6:  questionToSend = "What is the medicinal name for a hiccup?"; correctAnswer = "Hypernevrocousticdiaphragmaticcontravibrations";
                    break;
            }
        }

        public void newQuestion(){
            question();
            System.out.println("Current question is "+ questionToSend);
            for (PrintWriter writer : writers) {
                writer.println("QUESTION " + "Next question: " + questionToSend);
            }
        }
    }

    //USER CLASS
    public static class User extends Server{
        String userName="";
        int score=0;
        String hostName="";

        //Getters and setters
        public String getUserName(){
            return userName;
        }
        public int getScore(){
            return score;
        }
        public String getHostName(){
            return hostName;
        }
        public void setHostName(String x){
            hostName=x;
        }
        public void setUserName(String x){
            userName=x;
        }
        public void setScore(int x){
            score=x;
        }

        //Adds a new user
        public void addUser(String x, int y,String z){
            User newUser = new User();
            newUser.setUserName(x);
            newUser.setScore(y);
            newUser.setHostName(z);
            users.add(newUser);
            printUsers();
        }
        //Prints out userslist to users
        public void printUsers(){

            System.out.println("Userlist has been requested");
            String userList="";
            for(int i=0;i<users.size();i++){
                userList += users.get(i).getUserName() + " " + String.valueOf(users.get(i).getScore()) + ";";
            }
            System.out.println(userList);
            for (PrintWriter writer : writers) {
                System.out.println("Sending userlist");
                writer.println("USERUPDATE " + userList);
            }
        }

        //Adds a point to the user printing the correct answer
        public void addScore(String x){
            String userScored=x;
            System.out.println("The user who scored is: "+userScored);
            for(int i=0;i<users.size();i++){
                System.out.println("Loop started");
                System.out.println("Looked at "+users.get(i).getHostName());
                if(userScored.equals(users.get(i).getHostName())){
                    System.out.println("Someone was found");
                    int theScore = users.get(i).getScore();
                    theScore++;
                    users.get(i).setScore(theScore);
                    checkWinner();
                    break;
                }
            }
        }

        //Removes user from userlist
        public void removeUser(String x){
            String userToBeRemoved=x;
            for(int i=0;i<users.size();i++){
                if(users.get(i).getHostName().equals(userToBeRemoved)){
                    System.out.println("User "+ users.get(i).getUserName()+"has been removed");
                    users.remove(i);
                    printUsers();
                }
            }
        }

        public void checkWinner(){
            for(int i=0;i<users.size();i++){
                if(users.get(i).getScore()==10){
                    for (PrintWriter writer : writers) {
                        System.out.println("There is a winner!");
                        writer.println("MESSAGE " +users.get(i).getUserName()+ " has won the match! A new game begins...");
                        for(int y=0;y<users.size();y++){
                            users.get(y).setScore(0);
                            printUsers();
                        }
                    }
                }
            }
        }
    }
}

