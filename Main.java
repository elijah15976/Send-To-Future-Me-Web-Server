//import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
//import com.sun.net.httpserver.HttpsExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
//import java.io.*;
//import java.sql.*;
import com.sun.net.httpserver.*;
import java.net.InetSocketAddress;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.Arrays;
import java.security.*;

class Main {
  private static Logger lg = new Logger();
  public static void main(String[] args) throws Exception{
    int port = 8500;
    Database db = new Database("jdbc:sqlite:messages.db");
    
    HttpServer server = HttpServer.create(new InetSocketAddress(port),0);
    //messages table
    //time key, military time
    //privacy key, 1=private, 0=public
    //status key, 1=sent, 0=pending
    
    //users table
    //verified key, 0=unverified, 1=verified

    //---------------Verification Routes---------------
    String[][] unverifiedUsers = db.runSQL("SELECT * FROM Verification", 1);
    for(int i = 0; i<unverifiedUsers.length; i++){
      final int j = i;
      server.createContext("/verify/"+unverifiedUsers[i][1], new HttpHandler(){
        public void handle(HttpExchange exchange)throws IOException{
          exchange.getResponseHeaders().set("Content-Type", "text/html");
          String response;

          boolean status1 = db.runSQL("UPDATE Users SET verified=1 WHERE Email='"+unverifiedUsers[j][0]+"'");
          boolean status2 = db.runSQL("DELETE FROM Verification WHERE VerificationCode='"+unverifiedUsers[j][1]+"'");

          if(status1 && status2){
            response = "<h5 style='text-align:center;'>You have been verified</h5><h6 style='text-align:center;'>It is now safe to close the window</h6>";
          }
          else{
            response = "<h5 style='text-align:center;'>Something went wrong</h5><h6 style='text-align:center;'>Please try again</h6>";
          }
          
          RouteHandler.send(response, exchange, "/verify/"+unverifiedUsers[j][1]);
          if(status1 && status2){
            server.removeContext("/verify/"+unverifiedUsers[j][1]);
          }
        }
      });
    }
    
    //---------------Keep Alive Route---------------
    server.createContext("/keep-alive", new HttpHandler(){
      public void handle(HttpExchange exchange) throws IOException {

        Thread keepAliver = new Thread("keepAliver"){
          public void run(){
            HttpRequests req = new HttpRequests("https://email-manager.elijah15976.repl.co", "GET");
            try{
              HttpResponse<String> res = req.sendRequest();
            }
            catch(Exception e){
              lg.dispMessage("Keep Aliver bad :(", "warn");
            }
          }
        };
        keepAliver.start();
        
        RouteHandler.send("I have been awoken", exchange, "/keep-alive");
      }
    });
    //https://email-manager.elijah15976.repl.co
    
    //---------------Test Route---------------
    server.createContext("/test", new HttpHandler(){
      public void handle(HttpExchange exchange) throws IOException {
        Map<String, Object> parameters = RouteHandler.parseParameters("get",exchange);

        //ManagerCookie.setValue(exchange, "secret", "hello");
        
        RouteHandler.send("This is a test route, to test functionalities of the server before putting it into use", exchange, "/test");
      }
    });

    
    //---------------Website---------------
    server.createContext("/", new RouteHandler("Website/index.html", "text/html", "/"));
    server.createContext("/database", new RouteHandler("Website/database.html", "text/html", "/database"));
    server.createContext("/send", new RouteHandler("Website/send.html", "text/html", "/send"));
    server.createContext("/private", new RouteHandler("Website/private.html", "text/html", "/private"));
    server.createContext("/record", new RouteHandler("Website/record.html", "text/html", "/record"));
    server.createContext("/login", new RouteHandler("Website/login.html", "text/html", "/login"));
    server.createContext("/style.css", new RouteHandler("Website/style.css", "text/css", "/style.css"));
    server.createContext("/send.css", new RouteHandler("Website/send.css", "text/css", "/send.css"));
    server.createContext("/database.css", new RouteHandler("Website/database.css", "text/css", "/database.css"));
    server.createContext("/private.css", new RouteHandler("Website/private.css", "text/css", "/private.css"));
    server.createContext("/record.css", new RouteHandler("Website/record.css", "text/css", "/record.css"));
    server.createContext("/login.css", new RouteHandler("Website/login.css", "text/css", "/login.css"));
    server.createContext("/script.js", new RouteHandler("Website/script.js", "text/javascript", "/script.js"));
    server.createContext("/database.js", new RouteHandler("Website/database.js", "text/javascript", "/database.js"));
    server.createContext("/send.js", new RouteHandler("Website/send.js", "text/javascript", "/send.js"));
    server.createContext("/private.js", new RouteHandler("Website/private.js", "text/javascript", "/private.js"));
    server.createContext("/record.js", new RouteHandler("Website/record.js", "text/javascript", "/record.js"));
    server.createContext("/login.js", new RouteHandler("Website/login.js", "text/javascript", "/login.js"));
    server.createContext("/favicon.ico", new RouteHandler("Website/favicon.ico", "image/x-icon", "/favicon.ico"));

    
    //---------------DB Get API---------------
    server.createContext("/messages", new RouteHandler(db, "SELECT * FROM messages WHERE privacy='0'", "/messages"));


    //---------------DB Get API Complicated---------------
    server.createContext("/private-messages", new HttpHandler(){
      public void handle(HttpExchange exchange) throws IOException {
        String response;
        String account = secToUser(exchange, db, "/private-messages");
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        
        if((account.length()) > 1 && (!account.equals("success"))){
          response = db.runSQL("SELECT * FROM Messages WHERE Email='"+account+"'", "json");
        }
        else{
          response = "[]";
        }
        
        RouteHandler.send(response, exchange, "/private-messages");
      }
    });


    //---------------DB Get API Except Post---------------
    server.createContext("/unsent-messages", new HttpHandler(){
      public void handle(HttpExchange exchange) throws IOException {
        Map<String, Object> parameters = RouteHandler.parseParameters("post",exchange);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        String response;
        
        String secret = "";
        try{
          secret = parameters.get("secret").toString();

          if(secret.equals(System.getenv("email-manager_password"))){
            response = db.runSQL("SELECT * FROM Messages WHERE status=0", "json");
          }
          else{
            response = "[]";
          }
        }
        catch(Exception e){
          response = "[]";
        }

        RouteHandler.send(response, exchange, "/unsent-messages");
      }
    });


    //---------------Custom Get API---------------
    server.createContext("/secret-to-user", new HttpHandler(){
      public void handle(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "text/plain");

        String response = secToUser(exchange, db, "/secret-to-user");
        
        RouteHandler.send(response, exchange, "/secret-to-user");
      }
    });


    //---------------Post API---------------
    server.createContext("/post-msg", new HttpHandler(){
      public void handle(HttpExchange exchange) throws IOException {
        Map<String, Object> parameters = RouteHandler.parseParameters("post",exchange);
        String email, recipient, subject, message, date, time;
        String[] badDate = {null, null, null};
        try{
          email = parameters.get("email").toString();
          recipient = parameters.get("recipient").toString();
          subject = parameters.get("subject").toString();
          message = parameters.get("message").toString();
          date = parameters.get("date").toString();
          time = parameters.get("time").toString();
        }
        catch(Exception e){
          email = "";
          recipient = "";
          subject = "";
          message = "";
          date = "";
          time = "";
        }
        
        String response;
        String sql2 = "";
        String auth = secToUser(exchange, db, "/post-msg");
        if((auth.length() > 1) && (!auth.equals("success"))){
          if((email.equals(auth)) &&
             (!recipient.isEmpty()) &&
             (recipient.indexOf("@")!=-1 && recipient.indexOf(".")!=-1) &&
             (!subject.isEmpty()) &&
             (!message.isEmpty()) &&
             (!Arrays.equals(Format.formatDate(date, "M/D/Y"), badDate)) &&
             (!Arrays.equals(Format.formatTime(time, "H:M"), badDate)) &&
             (parameters.get("privacy") != null)){
            response = parameters.toString();
            sql2 = "INSERT INTO messages ('Email', 'Recipient', 'Sub', 'Msg', 'Date', 'Time', 'Privacy', 'Status') VALUES ('"+email.trim()+"', '"+recipient.trim()+"', '"+subject.trim()+"', '"+message.trim()+"', '"+date.trim()+"', '"+time.trim()+"', " + parameters.get("privacy").toString() + ", 0)";
            lg.dispMessage("SQL: " + sql2, "info");
            db.runSQL(sql2);
            Thread sendRequest = new Thread("addWaitingList"){
              public void run(){
                HttpRequests req = new HttpRequests("https://email-manager.elijah15976.repl.co/update", "GET");
                try{
                  HttpResponse<String> res = req.sendRequest();
                  if(res.body().equals("Critical Error")){
                    lg.dispMessage("Email Handler ran into an issue ~ /post-msg", "crit");
                  }
                }
                catch(Exception e){
                  lg.dispMessage(e.toString() + " ---- Main.java", "error");
                }
              }
            };
            sendRequest.start();
            String reply = db.runSQL("SELECT count(*) FROM messages", "csv");
            Input.writeFile("tablelength", reply.substring(reply.indexOf(",")+2, reply.length()-2));
          }
          else{
            response = "Bad Parameters";
          }
        }
        else{
          response = "Not Registered or Not Verified";
        }
        

        lg.dispMessage("Response: " + response, "info");
        RouteHandler.send(response, exchange, "/post-msg");
      }
    });
    server.createContext("/log-in", new HttpHandler(){
      public void handle(HttpExchange exchange) throws IOException {
        Map<String, Object> parameters = RouteHandler.parseParameters("post",exchange);
        exchange.getResponseHeaders().set("Content-Type", "text/plain");
        String response;
        String dbres = "";
        String email = "";
        String password = "";
        String num = "";
        String token = "";
        try{
          email = parameters.get("email").toString().trim();
          password = parameters.get("password").toString();
          num = parameters.get("num").toString();
        }
        catch(Exception e){
          email = "";
          password = "";
          num = "";
        }

        try{
          if((!email.isEmpty()) &&
             (!password.isEmpty()) &&
             (!num.isEmpty())){
            if(num.equals("0")){
              dbres = db.runSQL("SELECT Secret FROM users WHERE Email='"+email+"'", "csv");
              dbres = dbres.substring(dbres.indexOf(",")+2, dbres.length()-2);
              if(PasswordHasher.compareHash(dbres, password)){
                if(db.runSQL("SELECT Verified FROM users WHERE Email='"+email+"' AND Verified=1", "json").equals("[]")){
                  String verifyCode = db.runSQL("SELECT VerificationCode FROM Verification WHERE Email='"+email+"'", "csv");
                  verifyCode = verifyCode.substring(verifyCode.indexOf(",")+2, verifyCode.length()-2);
                  final String finalEmail = email;
                  final String finalCode = verifyCode;
                  Thread requestSend = new Thread("sendVerify"){
                      public void run(){
                        HttpRequests req = new HttpRequests("https://email-manager.elijah15976.repl.co/verification", "POST");
                        try{
                          HttpResponse<String> res = req.sendRequest("email="+finalEmail+"&code="+finalCode);
                          if(!res.body().equals("Email Sent")){
                            lg.dispMessage("Email Verification not sent ~ /log-in", "warn");
                          }
                        }
                        catch(Exception e){
                          lg.dispMessage(e.toString() + " ---- Main.java", "error");
                        }
                      }
                    };
                    requestSend.start();
                  response = "Email Not Found";
                  RouteHandler.send(response, exchange, "/log-in");
                  return;
                }
                if(db.runSQL("SELECT SessionToken FROM users WHERE Email='"+email+"'", "json").equals("[{\"SessionToken\":\"\"}]")){
                  String newSessionToken = randomLetters();
                  db.runSQL("UPDATE Users SET SessionToken='"+newSessionToken+"' WHERE Email='"+email+"'");
                }
                token = db.runSQL("SELECT SessionToken FROM users WHERE Email='"+email+"'", "csv");
                token = token.substring(token.indexOf(",")+2, token.length()-2);
                ManagerCookie.setValue(exchange, "secret", token);
                response = "success";
              }
              else{
                response = "Password Invalid";
              }
            }
            else if(num.equals("1")){
              if((email.indexOf("@") != -1) && (email.indexOf(".") != -1)){
                if(db.runSQL("SELECT Email FROM users WHERE Email ='"+email+"'", "json").equals("[]")){
                  lg.dispMessage("New User: "+email, "info");
                  boolean regStatus = db.runSQL("INSERT INTO users (Email, Secret, Verified) VALUES ('"+email+"', '"+PasswordHasher.hash(password)+"', '0')");
                  if(regStatus){
                    String code = randomLetters();
                    while(!db.runSQL("SELECT Email FROM Verification WHERE verificationCode='"+code+"'", "json").equals("[]")){
                      //System.out.println(db.runSQL("SELECT Email FROM Verification WHERE verificationCode='"+code+"'", "json"));
                      code = randomLetters();
                    }
                    final String sendEmail = email;
                    final String sendCode = code;
                    Thread sendRequest = new Thread("sendVerify"){
                      public void run(){
                        HttpRequests req = new HttpRequests("https://email-manager.elijah15976.repl.co/verification", "POST");
                        try{
                          HttpResponse<String> res = req.sendRequest("email="+sendEmail+"&code="+sendCode);
                          if(!res.body().equals("Email Sent")){
                            lg.dispMessage("Email Verification not sent ~ /log-in", "warn");
                          }
                        }
                        catch(Exception e){
                          lg.dispMessage(e.toString() + " ---- Main.java", "error");
                        }
                      }
                    };
                    sendRequest.start();
                    System.out.println(code);
                    boolean veriStatus = db.runSQL("INSERT INTO Verification (Email, VerificationCode) VALUES ('"+email+"', '"+code+"')");
                    server.createContext("/verify/"+sendCode, new HttpHandler(){
                      public void handle(HttpExchange exchange)throws IOException{
                        exchange.getResponseHeaders().set("Content-Type", "text/html");
                        String response;
              
                        boolean status1 = db.runSQL("UPDATE Users SET verified=1 WHERE Email='"+sendEmail+"'");
                        boolean status2 = db.runSQL("DELETE FROM Verification WHERE VerificationCode='"+sendCode+"'");
              
                        if(status1 && status2){
                          response = "<h5 style='text-align:center;'>You have been verified</h5><h6 style='text-align:center;'>It is now safe to close the window</h6>";
                        }
                        else{
                          response = "<h5 style='text-align:center;'>Something went wrong</h5><h6 style='text-align:center;'>Please try again</h6>";
                        }
                        
                        RouteHandler.send(response, exchange, "/verify/"+sendCode);
                        if(status1 && status2){
                          server.removeContext("/verify/"+sendCode);
                        }
                      }
                    });
                    if(veriStatus){
                      response = "User created";
                    }
                    else{
                      response = "Critical";
                    }
                  }
                  else{
                    response = "Internal Error";
                  }
                }
                else{
                  response = "User Already Registered";
                }
              }
              else{
                response = "Not a Valid Email";
              }
            }
            else{
              response = "Internal Error";
            }
          }
          else{
            response = "Bad Parameters";
          }
        }
        catch(StringIndexOutOfBoundsException e){
          if(num.equals("0")){
            lg.dispMessage(e.toString() + " ---- Main.java", "error");
            response = "Email Not Found";
          }
          else{
            response = "There is literally no way to get here. If you manage to get here, congratulations";
          }
        }
        catch(NoSuchAlgorithmException e){
          lg.dispMessage(e.toString() + " ---- Main.java", "crit");
          response = "Critical";
        }

        RouteHandler.send(response, exchange, "/log-in");
      }
    });
    server.createContext("/sent-message", new HttpHandler(){
      public void handle(HttpExchange exchange) throws IOException {
        Map<String, Object> parameters = RouteHandler.parseParameters("post",exchange);
        exchange.getResponseHeaders().set("Content-Type", "text/plain");
        String response = "";
        String secret, messageId, statusToChangeTo;

        try{
          secret = parameters.get("secret").toString();
          messageId = parameters.get("messageId").toString();
          statusToChangeTo = parameters.get("statusToChangeTo").toString();
        }
        catch(Exception e){
          secret = "";
          messageId = "";
          statusToChangeTo = "";
        }

        if((!messageId.isEmpty()) &&
           (!statusToChangeTo.isEmpty()) &&
           (secret.equals(System.getenv("email-manager_password")))){
          boolean success = db.runSQL("UPDATE Messages SET status="+statusToChangeTo+" WHERE ID="+messageId);
          if(success){
            Thread restartEmailChecker = new Thread("restartEmailChecker"){
              public void run(){
                HttpRequests req = new HttpRequests("https://email-manager.elijah15976.repl.co/update", "GET");
                try{
                  HttpResponse<String> res = req.sendRequest();
                  if(res.body().equals("Critical Error")){
                    lg.dispMessage("Email Handler ran into an issue ~ /post-msg", "crit");
                  }
                }
                catch(Exception e){
                  lg.dispMessage(e.toString() + " ---- Main.java", "error");
                }
              }
            };
            restartEmailChecker.start();
            response = "success";
          }
          else{
            response = "bad";
          }
        }
        else{
          response = "nothing given";
        }
        
        RouteHandler.send(response, exchange, "/sent-message");
      }
    });

    server.start();
    lg.dispMessage("Server is listening on port "+ port, "info");
  }
  

  public static String secToUser(HttpExchange exchange, Database db, String route){
    //NOTE: Function already checks for verification
    String response;
    String checkLogOut;
    checkLogOut = ManagerCookie.getValue(exchange, "logOut");
    //lg.dispMessage(checkLogOut, "info");
    try{
      if(!checkLogOut.equals("1")){
        throw null;
      }
      else{
        //Run SQL to delete SessionToken from expected account
        //ALTERNATIVE: Reset Session Token every month
        response = "success";
      }
    }
    catch(Exception e){
      try{
        String cookieValue = ManagerCookie.getValue(exchange, "secret");
        if(cookieValue == null){
          response = "";
        }
        else{
          String email = db.runSQL("SELECT email FROM users WHERE Verified=1 AND SessionToken='" + cookieValue +"'", "csv");
          response = email.substring(email.indexOf(",")+2, email.length()-2);
        }
        lg.dispMessage("Response: " + response, "info");
      }
      catch(Exception egg){
        response = "";
        lg.dispMessage(egg.toString() + " ~ " + route, "warn");
      }
    }
    
    return response;
  }

  public static String randomLetters(){
    SecureRandom r = new SecureRandom();
    char[] codeLetters = new char[12];
    for(int i = 0; i<codeLetters.length; i++){
      codeLetters[i] = (char)(r.nextInt(25) + 'a');
    }
    return new String(codeLetters);
  }
}