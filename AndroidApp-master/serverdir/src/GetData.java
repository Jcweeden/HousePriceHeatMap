package heatmap;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

public class GetData extends HttpServlet 
{
	private static final long serialVersionUID = 1L;
	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
    static final String DB_URL = "jdbc:mysql://ase1sussex.ccfsoqbhse86.us-west-2.rds.amazonaws.com:3306/ase1";
    static final String DB_NAME = "ase1";
    static final String USER = "user";
    static final String PASS = "password";
    
    DataSource ds;
    Connection conn;
    Statement stat;
    ResultSet rs;
    String sql;
      
    // Release servlet resources at the end of execution or beginning of a new execution
    @Override
    public void destroy()
    {
    	super.destroy();
    	
        try 
        {
            if (rs != null)
                rs.close();
            if (stat != null)
                stat.close();
            if (conn != null)
                conn.close();
        }
        catch (Exception se) 
        {
            se.printStackTrace();
        }
    }
    
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{		
	    String radius = request.getParameter("radius");
	    String factor = request.getParameter("factor");
	    String postcode = null;
	    double latitude = 0;
	    double longitude = 0;
	    List<pointData> points = new ArrayList<pointData>();
	    pointData point = null;
	    
	    if(request.getParameter("typeSend").equals("P"))
	    {
	    	postcode = request.getParameter("postcode");
		    sql = "SELECT t.*, @rownum := @rownum + 1 AS rank "
		    		+ "FROM ( "
		    		+ "SELECT  postcode, latitude, longitude, price as weight "
		    		+ "FROM price_by_postcode AS z "
		    		+ "JOIN (SELECT latitude as latpoint, longitude as longpoint, " + radius + " AS radius, 111.045 AS distance_unit "
		    			+ "FROM price_by_postcode "
		    			+ "WHERE postcode like '" + postcode + "%' limit 1) AS p ON 1=1 "
		    		+ "WHERE z.latitude BETWEEN p.latpoint  - (p.radius / p.distance_unit) "
		    			+ "AND p.latpoint  + (p.radius / p.distance_unit) "
		    			+ "AND z.longitude BETWEEN p.longpoint - (p.radius / (p.distance_unit * COS(RADIANS(p.latpoint)))) "
		    			+ "AND p.longpoint + (p.radius / (p.distance_unit * COS(RADIANS(p.latpoint)))) "
		    		+ ") t, (SELECT @rownum := 0) r "
		    		+ "where (@rownum := @rownum + 1)%" + factor + "=0";
	    }
	    else
	    {
	    	latitude = Double.valueOf(request.getParameter("latitude"));
	    	longitude = Double.valueOf(request.getParameter("longitude"));
	    	sql = "SELECT t.*, @rownum := @rownum + 1 AS rank "
		    		+ "FROM ( "
	    			+ "SELECT  postcode, latitude, longitude, price as weight "
                    + "FROM price_by_postcode AS z "
                    + "JOIN (SELECT  " + latitude + "  AS latpoint, " + longitude + " AS longpoint," + radius + " AS radius, 111.045 AS distance_unit) AS p ON 1=1 "
                    + "WHERE z.latitude BETWEEN p.latpoint  - (p.radius / p.distance_unit) "
                    + "AND p.latpoint  + (p.radius / p.distance_unit) "
                    + "AND z.longitude BETWEEN p.longpoint - (p.radius / (p.distance_unit * COS(RADIANS(p.latpoint)))) "
                    + "AND p.longpoint + (p.radius / (p.distance_unit * COS(RADIANS(p.latpoint))))"
                    + ") t, (SELECT @rownum := 0) r "
		    		+ "where (@rownum := @rownum + 1)%" + factor + "=0";;
	    }
	    
	    try
        {
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL,USER,PASS);
            stat = conn.createStatement();
            rs = stat.executeQuery(sql);
            if (!rs.next())
            {
                System.out.println("13");
            }
            else 
            {
                rs.beforeFirst();
                while(rs.next()) 
                {
                	point = new pointData();
                	point.setLatitude(rs.getDouble("latitude"));
                	point.setLongitude(rs.getDouble("longitude"));
                	point.setWeight(rs.getDouble("weight"));
                	points.add(point);
                }
            }
        }
        catch (Exception e)
        {
            System.out.println("SQLException");
            e.printStackTrace();
        }
	    
	    String json = new com.google.gson.Gson().toJson(points);
	    response.setContentType("application/json");
	    response.setCharacterEncoding("UTF-8");
	    response.getWriter().write(json);
	    
//	    response.setContentType("text/plain");  // Set content type of the response so that jQuery knows what it can expect.
//	    response.setCharacterEncoding("UTF-8"); // You want world domination, huh?
//	    response.getWriter().write(json);       // Write response body.
	}

}
