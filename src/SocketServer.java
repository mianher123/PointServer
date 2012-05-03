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
        String UserId,ShopId,Pwd;
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
                else if(data.startsWith("VERIFY")){
                	String[] part = data.split(" ");
                	ShopId=part[1];
                	Pwd=part[2];
                	
                	String result = verify(ShopId,Pwd);
                	out.write(("VERIFYRESULT "+result).getBytes());
                	out.flush();
                	out.close();
                	out = null;
                	System.out.println("VERIFY finished");
                }
                else if(data.startsWith("REGISTER")){
                	String[] part = data.split(" ");
                	UserId=part[1];
                	
                	register(UserId);
                	
                	out.close();
                	out = null;
                	System.out.println("VERIFY finished");
                }
                else if(data.startsWith("FBLIST")){
                	String[] part = data.split(" ");
                	String s="";
                	for(int i=1;i<part.length;i++){
                		if (checkUserList(part[i]).equals("true")){
                			s +=(part[i]+" ");
                			
                		}
                		
                	}
                	out.write(("FBLISTRESULT "+s).getBytes());
                	out.flush();
                	out.close();
                	out = null;
                	System.out.println("FBLIST finished");
                }
                else if(data.startsWith("EXCHANGE")){
                	String[] part = data.split(" ");
                	String result = exchange(part[1],part[2],part[3],part[4]);
                	out.write(("EXCHANGERESULT "+result).getBytes());
                	out.flush();
                	out.close();
                	out = null;
                	System.out.println("EXCHANGE finished");
                }
                else if(data.startsWith("REFRESH")){
                	String[] part = data.split(" ");
                	System.out.println("ID = "+part[1]);
                	String result = refresh(part[1]);
                	
                	out.write(("REFRESHRESULT "+result).getBytes());
                	out.flush();
                	out.close();
                	out = null;
                	System.out.println("REFRESH finished");
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
    private String refresh(String userId) {
    	Connection con = null;
    	  String output="";
    	    try {
    	      Class.forName("sun.jdbc.odbc.JdbcOdbcDriver") ;
    	      con = DriverManager.getConnection("jdbc:odbc:mianher2");
    	      System.out.println("DSN Connection ok.");
    	      
    	      Statement stmt = con.createStatement();
    	      Statement stmt2 = con.createStatement();
    	      ResultSet rs = stmt.executeQuery("SELECT TableName FROM ShopAccount");
    	      while(rs.next()){
    	    	  String TableName = rs.getString("TableName");
    	    	  ResultSet rs1 = stmt2.executeQuery("SELECT Points FROM "+TableName+" where UserId='"+userId+"'");
    	    	  int num;
    	    	  if(!rs1.next()){
    	    		  num = 0;
    	    	  }
    	    	  else num = rs1.getInt("Points");
    	    	  
    	    	  output+=(TableName.replaceAll(" ", "")+" "+num+" ");
    	    	  
    	      }
    	      System.out.println("output = "+output);
    		  con.close();

    	    } catch (Exception e) {
    	      System.err.println("Exception: "+e.getMessage());
    	    }
    	    
    	    return output;
	}

	private String exchange(String myId, String yourId, String ShopName, String NumPoints) {
    	Connection con = null;
    	  try {
    	      Class.forName("sun.jdbc.odbc.JdbcOdbcDriver") ;
    	      con = DriverManager.getConnection("jdbc:odbc:mianher2");
    	      System.out.println("DSN Connection ok.");
    	      Statement stmt = con.createStatement();
    	      ResultSet rs = stmt.executeQuery("SELECT Points FROM "+ShopName+" where UserId='"+myId+"'");
    	      System.out.println("0");
    	      if(!rs.next()){
    	    	  System.out.println("9");
    	    	  con.close();
    	    	  return "false";
    	      } 
    	      System.out.println("8");
    	      int myPoints = rs.getInt("Points");
    	      System.out.println("7");
    	      System.out.println("myPoints = "+myPoints);
    	      int numPoints = Integer.parseInt(NumPoints);
    	      int flag=0;
    	      //System.out.println(TableName);
    	      if(myPoints < Integer.parseInt(NumPoints)){
    	    	  con.close();
    	    	  return "false";
    	      }
    	      
    	      
    	      else{
    	    	  rs = stmt.executeQuery("SELECT Points FROM "+ShopName+" where UserId='"+yourId+"'");
    	    	  int yourPoints=0;
    	    	  if(rs.next()){
    	    		  System.out.println("1");
            	      yourPoints = rs.getInt("Points"); 
            	      flag = 1;
        	      }
        	      
    	    	  stmt.executeUpdate("UPDATE "+ShopName+" SET Points='"+(myPoints-numPoints)+"' WHERE UserId = '"+myId+"'");
    	    	  System.out.println("2");
    	    	  if(flag == 1)
    	    		  stmt.executeUpdate("UPDATE "+ShopName+" SET Points='"+(yourPoints+numPoints)+"' WHERE UserId = '"+yourId+"'");
    	    	  else
    	    		  stmt.executeUpdate("INSERT INTO "+ShopName +  " VALUES ("+yourId+", "+(yourPoints+numPoints)+")"); 
    	    	  System.out.println("3");
    	    	  System.out.println("INSERT INTO ExchangeList VALUES ('"+ShopName+"', "+myId+", "+yourId+", "+NumPoints+", '"+now()+"')");
    	    	  stmt.executeUpdate("INSERT INTO ExchangeList VALUES ('"+ShopName+"', "+myId+", "+yourId+", "+NumPoints+", '"+now()+"')");
    	    	  System.out.println("4");
    	    	  con.close();
    	    	  return "true";
    	      }
    	      
    		  

    	    } catch (Exception e) {
    	    	System.err.println("Exception: "+e.getMessage());
    	      	return "false";
    	    }
    	    
    	  
	}

	private String checkUserList(String userId) {
    	Connection con = null;
		try {
	      Class.forName("sun.jdbc.odbc.JdbcOdbcDriver") ;
	      con = DriverManager.getConnection("jdbc:odbc:mianher2");
	      System.out.println("DSN Connection ok.");
	      Statement stmt = con.createStatement();
	      ResultSet rs = stmt.executeQuery("SELECT UserId FROM UserList where UserId='"+userId+"'");
	      
	      if (rs.next()){
	    	  System.out.println(rs.getString("UserId"));
	    	  con.close();
	    	  return "true";
	      }
	      else{
	    	  con.close();
	    	  return "false";
	      }

	    } catch (Exception e) {
	      System.err.println("Exception: "+e.getMessage());
	      return "ERROR";
	    }
	}

	private void register(String userId){
    	  Connection con = null;
    	    try {
    	      Class.forName("sun.jdbc.odbc.JdbcOdbcDriver") ;
    	      con = DriverManager.getConnection("jdbc:odbc:mianher2");
    	      System.out.println("DSN Connection ok.");
    	      Statement stmt = con.createStatement();
    	      stmt.executeUpdate("INSERT INTO UserList VALUES ("+userId+", '"+now()+"')"); 
    		  con.close();

    	    } catch (Exception e) {
    	      System.err.println("Exception: "+e.getMessage());
    	    }
    	  
      }
    private String verify(String shopId, String pwd) {
    		Connection con = null;
    		try {
    	      Class.forName("sun.jdbc.odbc.JdbcOdbcDriver") ;
    	      con = DriverManager.getConnection("jdbc:odbc:mianher2");
    	      System.out.println("DSN Connection ok.");
    	      String TableName;
    	      Statement stmt = con.createStatement();
    	      ResultSet rs = stmt.executeQuery("SELECT TableName FROM ShopAccount where ShopId='"+shopId+"' AND Pwd='"+pwd+"'");
    	      rs.next();
    	      TableName = rs.getString("TableName");
    	      con.close();
    	      if(TableName!=null){
    	    	  System.out.println(TableName);
    	    	  return TableName;
    	      }
    	      else
    	    	  return "ERROR";
    		  

    	    } catch (Exception e) {
    	      System.err.println("Exception: "+e.getMessage());
    	      return "ERROR";
    	    }
    	    
	}

	public static void main(String args[]) {
		//newTable("ExchangeList");
		//newshop("FAMILY", "0005", "1234");
		
        (new SocketServer()).start();
    }
	
	public static void newTable(String TableName){
	  	  Connection con = null;
	  	    try {
	  	      Class.forName("sun.jdbc.odbc.JdbcOdbcDriver") ;
	  	      con = DriverManager.getConnection("jdbc:odbc:mianher2");
	  	      System.out.println("DSN Connection ok.");
	  	      Statement stmt = con.createStatement();
	  	      //stmt.execute("CREATE TABLE "+ShopName+"(UserId CHAR(25) NOT NULL, Points CHAR(25), primary key(UserId))");
	  	      stmt.execute("CREATE TABLE "+TableName+"(ShopName CHAR(25) NOT NULL, SId CHAR(25) NOT NULL, RId CHAR(25) NOT NULL, Points CHAR(25), Timing CHAR(25), primary key(Timing))");
	  	      //stmt.executeUpdate("INSERT INTO ShopAccount VALUES("+ShopId+","+Pwd+",'"+ShopName+"')"); 
	  	      //stmt.execute("CREATE TABLE ShareHistory (HistoryId INTEGER NOT NULL, SenderId CHAR(25) NOT NULL, ReceiverId CHAR(25) NOT NULL, primary key(HistoryId))");
	  		  con.close();

	  	    } catch (Exception e) {
	  	      System.err.println("Exception: "+e.getMessage());
	  	    }
	  	  
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