import java.io.BufferedInputStream;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.io.BufferedOutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
 
public class SocketServer extends java.lang.Thread {
 
    private boolean OutServer = false;
    private ServerSocket server;
    private final int ServerPort = 5566;// server port
    public static final String DATE_FORMAT_NOW = "yyyy/MM/dd-HH:mm:ss";
    
    public SocketServer() {
        try {
            server = new ServerSocket(ServerPort);
 
        } catch (java.io.IOException e) {
            System.out.println("Socket error!");
            System.out.println("IOException :" + e.toString());
        }
    }
 
    public void run() {
        Socket socket;
        BufferedInputStream in;
        BufferedOutputStream out;
        String UserId,ShopId;
        String info;
        int numPoints=0;
        System.out.println("server starts!");
        while (!OutServer) {
            socket = null;
            try {
                synchronized (server) {
                    socket = server.accept();
                }
                System.out.println("Client's InetAddress = " + socket.getInetAddress());
                // TimeOut��
                socket.setSoTimeout(15000);
 
                in = new BufferedInputStream(socket.getInputStream());
                System.out.println("input buffer is opened");
                out = new BufferedOutputStream(socket.getOutputStream());
                System.out.println("output buffer is opened");
                byte[] b = new byte[1024];
                String data = "";
                int length;
                //while ((length = in.read(b)) > 0)// <=0�店撠望蝯�鈭�
                length = in.read(b);
                data += new String(b, 0, length);
               
 
                System.out.println("Receive: " + data);
                /*in.close();
                in = null;*/
                
                if(data.startsWith("REC")){
                	String[] part = data.split(" ");
                	UserId=part[1];
                	ShopId=part[2];
                	numPoints=Integer.parseInt(part[3]);
                	info=pushRec(ShopId, UserId,""+numPoints);
                	
                
                	out.write(("INFO "+info).getBytes());
                	out.flush();
                	out.close();
                	out = null;
                	System.out.println("REC finished");
                }
                /*
                Socket client = new Socket();              
                InetSocketAddress isa = new InetSocketAddress(socket.getInetAddress(), 7788);
                
                client.connect(isa, 10000);
                BufferedOutputStream out = new BufferedOutputStream(client
                            .getOutputStream());
                out.write("Send From Server ".getBytes());
                out.flush();
                out.close();
                out = null;
                */
                
                
                socket.close();
                System.out.println("socket is closed");
 
            } catch (java.io.IOException e) {
                System.out.println("Socket error!");
                System.out.println("IOException :" + e.toString());
            }
 
        }
    }
 
    public static void main(String args[]) {
        (new SocketServer()).start();
    }
    public static void newshop(String ShopName, String ShopId, String Pwd){
  	  Connection con = null;
  	    try {
  	      Class.forName("sun.jdbc.odbc.JdbcOdbcDriver") ;
  	      con = DriverManager.getConnection("jdbc:odbc:mianher2");
  	      System.out.println("DSN Connection ok.");
  	      Statement stmt = con.createStatement();
  	      stmt.execute("CREATE TABLE "+ShopName+"(UserId CHAR(25) NOT NULL, Points CHAR(25), primary key(UserId))");
  	      stmt.execute("CREATE TABLE H_"+ShopName+"(UserId CHAR(25) NOT NULL, Points CHAR(25), Timing CHAR(25), primary key(UserId, Timing))");
  	      stmt.executeUpdate("INSERT INTO ShopAccount VALUES("+ShopId+","+Pwd+",'"+ShopName+"')"); 
  	      //stmt.execute("CREATE TABLE ShareHistory (HistoryId INTEGER NOT NULL, SenderId CHAR(25) NOT NULL, ReceiverId CHAR(25) NOT NULL, primary key(HistoryId))");
  		  con.close();

  	    } catch (Exception e) {
  	      System.err.println("Exception: "+e.getMessage());
  	    }
  	  
    }
    
    public static String pushRec(String ShopId, String UserId, String numPoints){
  	  Connection con = null;
  	  int num=0;
  	    try {
  	      Class.forName("sun.jdbc.odbc.JdbcOdbcDriver") ;
  	      con = DriverManager.getConnection("jdbc:odbc:mianher2");
  	      System.out.println("DSN Connection ok.");
  	      String TableName;
  	      Statement stmt = con.createStatement();
  	      ResultSet rs = stmt.executeQuery("SELECT TableName FROM ShopAccount where ShopId='"+ShopId+"'");
  	      rs.next();
  	      TableName = rs.getString("TableName");
  	      System.out.println(TableName);
  	      
  	      rs = stmt.executeQuery("SELECT Points FROM "+TableName+" where UserId='"+UserId+"'");
  	      if(!rs.next())
  	    	  num=Integer.parseInt(numPoints);
  	      else
  	    	  num=rs.getInt("Points")+Integer.parseInt(numPoints);
  	      
  	      try{stmt.executeUpdate("INSERT INTO "+TableName +  " VALUES ("+UserId+", "+num+")"); }
  	      catch(Exception e){
  	    	  //System.err.println("Exception: "+e.getMessage());
  	    	  stmt.executeUpdate("UPDATE "+TableName+" SET Points='"+num+"' WHERE UserId = '"+UserId+"'");
  	    	  
  	      }
  	      
  	      stmt.executeUpdate("INSERT INTO H_"+TableName +  " VALUES ("+UserId+", "+numPoints+", '"+now()+"')"); 
  		  con.close();

  	    } catch (Exception e) {
  	      System.err.println("Exception: "+e.getMessage());
  	    }
  	    return ShopId+" "+UserId+" "+num;
  	  
    }
    public static String now() {
  	    Calendar cal = Calendar.getInstance();
  	    SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
  	    return sdf.format(cal.getTime());

  	  }
}