package heatmap;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.Locale;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

// Servlet to process the information from the database and produce the data to generate the heatmap
public class TestMapServlet extends HttpServlet
{
    // Database information
    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
    static final String DB_URL = "jdbc:mysql://ase1sussex.ccfsoqbhse86.us-west-2.rds.amazonaws.com:3306/ase1";
    static final String DB_NAME = "ase1";
    static final String USER = "user";
    static final String PASS = "password";
    
    InitialContext ctx;
    DataSource ds;
    Connection conn;
    Statement stat;
    ResultSet rs;
    
    String maptype;
    String postcode;
    String typeFind;
    double latParam;
    double lonParam;
    double latitudeMin;
    double latitudeMax;
    double longitudeMin;
    double longitudeMax;
    int maxZoom;
    int minZoom;
    int zoom;
    int radius; 
    StringBuilder mapMarkers;
    String sql;
    String faction;
    
    // Initializes the variables
    public void variablesInitialization()
    {
        maptype = "";
        postcode = "";
        typeFind = "";
        latParam = 0.0;
        lonParam = 0.0;
        latitudeMin = 0.0;
        latitudeMax = 0.0;
        longitudeMin = 0.0;
        longitudeMax = 0.0;
        maxZoom = 19;
        minZoom = 6;
        zoom = 19;
        radius = 1;
        sql = "";
        faction = "";
        mapMarkers = new StringBuilder();
    }
    
    // Set appropriate query to retrieve data
    public void queryData(HttpServletRequest req)
    {
        // get the parameters entered in the url.
        maptype  = req.getParameter("maptype");
        typeFind = req.getParameter("typeFind");
            
        // Setup the query based on the type request
        if(typeFind.equals("P")) // P = Postcode
        {
            //parameter from url
            postcode = req.getParameter("postcode");
            
            // set the sql query to retrieve data
            sql = "SELECT postcode, latitude, longitude, price as weight "
                    + "FROM price_by_postcode "
                    + "WHERE postcode like '" + postcode + "%'";
            
            // set the correct zoom level based on the length of the postcode entered.
            switch (postcode.length()) 
            {
                case 1: 
                    zoom = 9;
                    // look for postcodes with one alphabetic character followed by a digit.
                    sql = "SELECT postcode,latitude, longitude, price as weight "
                            + "FROM price_by_postcode "
                            + "WHERE postcode rlike '^" + postcode + "[0-9]'";
                    break;
                case 2: 
                    zoom = 10;
                    break;
                case 3: 
                    zoom = 12;
                    break;
                case 4:
                    zoom = 15;
                    break;
                case 5: 
                    zoom = 17;
                    break;
                default:
                    zoom = 19;
                    // set the sql for min zoom shows the postcodes in the radious area
                    sql = "SELECT  postcode, latitude, longitude, price as weight "
                            + "FROM price_by_postcode AS z "
                            + "JOIN (SELECT latitude as latpoint, longitude as longpoint, " + radius + " AS radius, 111.045 AS distance_unit "
                                    + "FROM price_by_postcode "
                                    + "WHERE postcode like '" + postcode + "%' limit 1) AS p ON 1=1 "
                            + "WHERE z.latitude BETWEEN p.latpoint  - (p.radius / p.distance_unit) "
                                + "AND p.latpoint  + (p.radius / p.distance_unit) "
                                + "AND z.longitude BETWEEN p.longpoint - (p.radius / (p.distance_unit * COS(RADIANS(p.latpoint)))) "
                                + "AND p.longpoint + (p.radius / (p.distance_unit * COS(RADIANS(p.latpoint))))";
                    break;
            }
        }
        else if(typeFind.equals("L")) // L = Location
        {
            // parameters from url
            latParam = Double.valueOf(req.getParameter("latitude"));
            lonParam = Double.valueOf(req.getParameter("longitude"));
            
            // set the sql for min zoom shows the postcodes in the radious area
            sql = "SELECT  postcode, latitude, longitude, price as weight "
                    + "FROM price_by_postcode AS z "
                    + "JOIN (SELECT  " + latParam + "  AS latpoint, " + lonParam + " AS longpoint," + radius + " AS radius, 111.045 AS distance_unit) AS p ON 1=1 "
                    + "WHERE z.latitude BETWEEN p.latpoint  - (p.radius / p.distance_unit) "
                    + "AND p.latpoint  + (p.radius / p.distance_unit) "
                    + "AND z.longitude BETWEEN p.longpoint - (p.radius / (p.distance_unit * COS(RADIANS(p.latpoint)))) "
                    + "AND p.longpoint + (p.radius / (p.distance_unit * COS(RADIANS(p.latpoint))))";
        }
    }
    
    // Initial servlet method
    @Override
    public void init(ServletConfig config) throws ServletException
    {
        super.init(config);
        
        variablesInitialization();
        
        try 
        {
            // Register JDBC driver
            Class.forName(JDBC_DRIVER);
            // Open a connection
            conn = DriverManager.getConnection(DB_URL,USER,PASS);
            // Get the name of the database and the form action from the context file
            ctx = new InitialContext();
            faction = "http://lowcost-env.kdumcfjv2e.us-west-2.elasticbeanstalk.com/TestMapServlet"; //server
            //faction = "http://localhost:8093/TestMapServlet/TestMapServlet"; //local
            // setup a Datasource.
            ds = (DataSource) ctx.lookup(DB_NAME);
            conn = ds.getConnection();
            stat = conn.createStatement();
        }
        catch (Exception se) 
        {
            se.printStackTrace();
        }
    }

    // Release servlet resources at the end of execution or beginning of a new execution
    @Override
    public void destroy()
    {
    	super.destroy();
        variablesInitialization();
        
        try 
        {
            if (rs != null)
                rs.close();
            if (stat != null)
                stat.close();
            if (conn != null)
                conn.close();
            if (ctx != null)
                ctx.close();
        }
        catch (SQLException se) 
        {
            se.printStackTrace();
        }
        catch (NamingException ne) 
        {
            ne.printStackTrace();
        }
    }

    // Response method of page call GET
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
    	resp.setContentType("text/html");
        PrintWriter out = new PrintWriter(resp.getOutputStream());
        
        // variable initialization
        //variablesInitialization();
        postcode = "";
        typeFind = "";
        mapMarkers = new StringBuilder();
            
        // set appropriate query to retrieve data based in url parameters
        queryData(req);

        // html content
        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("   <head>");
        out.println("       <meta charset='utf-8'>");
        out.println("       <title>Heat maps</title>");
        out.println("       <style>");
        out.println("           #map");
        out.println("           {");
        out.println("               height: 100%;"); //define the size of the div element that contains the map.
        out.println("           }");
        out.println("           html, body");
        out.println("           {");
        out.println("               height: 100%;"); // page fill the window. 
        out.println("               margin: 0;");
        out.println("               padding: 0;");
        out.println("           }");
        out.println("           #floating-panel");
        out.println("           {");
        out.println("               width:80px;");
        out.println("               background-color: #fff;");
        out.println("               border: 1px solid #999;");
        out.println("               right: 5%;");
        out.println("               padding: 5px;");
        out.println("               position: absolute;");
        out.println("               top: 10px;");
        out.println("               z-index: 5;");
        out.println("               text-align: center;");
        out.println("               font-family: 'Roboto','sans-serif';");
        out.println("               line-height: 30px;");
        out.println("               padding-left: 10px;");
        out.println("           }");
        out.println("           #floating-panel-temperature");
        out.println("           {");
        out.println("               width:80px;");
        out.println("               background-color: #fff;");
        out.println("               border: 1px solid #999;");
        out.println("               left: 2%;");
        out.println("               padding: 5px;");
        out.println("               position: absolute;");
        out.println("               bottom: 10px;");
        out.println("               z-index: 5;");
        out.println("               text-align: center;");
        out.println("               font-family: 'Roboto','sans-serif';");
        out.println("               line-height: 30px;");
        out.println("               padding-left: 10px;");
        out.println("           }");
        out.println("           #heatBar");
        out.println("           {");
        out.println("           	background: -webkit-gradient(linear, left top, left bottom, color-stop(0%, rgba(250,0,0,1)), color-stop(11%, rgba(255,119,0,0.99)), color-stop(23%, rgba(245,152,12,0.99)), color-stop(35%, rgba(240,240,93,0.99)), color-stop(47%, rgba(240,240,93,0.98)), color-stop(59%, rgba(0,250,58,0.97)), color-stop(69%, rgba(0,250,58,0.96)), color-stop(80%, rgba(35,173,14,0.92)), color-stop(90%, rgba(35,173,14,0.85)), color-stop(100%, rgba(35,173,14,0.7)));");
        out.println("           	width:20px;");
        out.println("           	height:100px;");
        out.println("           }");
        out.println("       </style>");
        out.println("   </head>");
        out.println("   <body>");
        // form newPostcode is posted back to the servlet with the new maptype and postcode selected 
        out.println("       <form id='newPostcode' action='" + faction + "' method='POST' accept-charset='UTF-8'>");
        out.println("           <input type='hidden' name='maptype' value='" + maptype + "'>");
        out.println("           <input type='hidden' name='postcode' value='" + postcode + "'>");
        out.println("           <input type='hidden' name='typeFind' value='P'>");
        out.println("       </form>");
        out.println("       <div id='floating-panel'>");
        out.println("           Postcode: <input type='text' id='pcode' style='width: 70px;' value='" + postcode + "'>");
        out.println("           <button onclick='showMap()'>Send</button>");
        out.println("       </div>");
        out.println("       <div id='floating-panel-temperature'>");
        out.println("       	<table id='tabula'>");
        out.println("       		<tr>");	
        out.println("       			<td rowspan=2><div id='heatBar'></div></td>");
        out.println("       			<td style='vertical-align:top;'>0.0</td>");
        out.println("       		</tr>");
        out.println("       		<tr>");	
        out.println("       			<td style='vertical-align:bottom;'>0.0</td>");
        out.println("       		</tr>");
        out.println("       	</table>");
        out.println("       </div>");
        out.println("       <div id='map'></div>");
        // generates heatmap
        out.println("       <script type='text/javascript' src='https://ajax.googleapis.com/ajax/libs/jquery/3.1.1/jquery.min.js'></script>");
        out.println("       <script type='text/javascript'>");
        out.println("           var map, heatmap;");
        out.println("           var markerCount = 0;");
        out.println("           var markers = [];");
        out.println("           var markersGenerator = [];");
        out.println("           var latitudeMin, latitudeMax, longitudeMin, longitudeMax;");
        out.println("           function initMap()");
        out.println("           {");
        out.println("               map = new google.maps.Map(document.getElementById('map'), ");
        out.println("               {");
        out.println("                   center: {lat: " + latParam + ", lng: " + lonParam + "},");
        out.println("                   mapTypeId: 'roadmap',");
        out.println("                   maxZoom: " + String.valueOf(maxZoom) + ",");
        out.println("                   minZoom: " + String.valueOf(minZoom) + ",");
        out.println("               });");
        out.println("               map.addListener('zoom_changed', function(){zoomChanged()});");
        out.println("               heatmap = new google.maps.visualization.HeatmapLayer");
        out.println("               ({");
        out.println("                   data: getPoints(),");
        out.println("                   radius: 250,");
        //out.println("                     dissipating: false,");
        out.println("                   opacity: 0.7,");
        out.println("                   gradient: [ 'rgba(35,173,14,0.0)',"); //35,173,14//0,15,112
        out.println("                               'rgba(35,173,14,0.63)' ,");
        out.println("                               'rgba(35,173,14,0.64)' ,");
        out.println("                               'rgba(0,250,58,0.65)' ,");//0,250,58//0,171,250
        out.println("                               'rgba(0,250,58,0.66)' ,");
        out.println("                               'rgba(240,240,93,0.68)' ,");
        out.println("                               'rgba(240,240,93,0.68)' ,");
        out.println("                               'rgba(245,152,12,0.68)' ,");
        out.println("                               'rgba(255,119,0,0.69)' ,");
        out.println("                               'rgba(255,0,0,0.7)' ],");
        out.println("                   map: map");
        out.println("               });");
        // adds the markers
        if(typeFind.equals("L") || (typeFind.equals("P") && zoom == 19))
        {
            out.println("           for (var i = 0; i < markersGenerator.length; i++)");
            out.println("           {");
            out.println("               addMarkerToMap(markersGenerator[i].latitude, markersGenerator[i].longitude, markersGenerator[i].content);");
            out.println("           }");
            out.println("           map.setCenter(new google.maps.LatLng((latitudeMin + latitudeMax)/2,(longitudeMin + longitudeMax)/2));");
            out.println("           map.setZoom(" + String.valueOf(maxZoom) + ");");
            out.println("   }");
        }
        else
        {
            out.println("           map.setCenter(new google.maps.LatLng((latitudeMin + latitudeMax)/2,(longitudeMin + longitudeMax)/2));");
            out.println("           map.setZoom(" + String.valueOf(zoom) + ");");
            out.println("           markers = [];");
            out.println("   }");
        }
        out.println("");
        out.println("           function initMapStrikesBack(arrayPoints, radHeatParam)");
        out.println("           {");
        out.println("           	heatmap.setMap(null);");
        out.println("               heatmap = new google.maps.visualization.HeatmapLayer");
        out.println("               ({");
        out.println("                   data: arrayPoints,");
        out.println("                   radius: radHeatParam,");
        out.println("                   opacity: 0.7,");
        out.println("                   gradient: [ 'rgba(35,173,14,0.0)',"); //35,173,14//0,15,112
        out.println("                               'rgba(35,173,14,0.63)' ,");
        out.println("                               'rgba(35,173,14,0.64)' ,");
        out.println("                               'rgba(0,250,58,0.65)' ,");//0,250,58//0,171,250
        out.println("                               'rgba(0,250,58,0.66)' ,");
        out.println("                               'rgba(240,240,93,0.68)' ,");
        out.println("                               'rgba(240,240,93,0.68)' ,");
        out.println("                               'rgba(245,152,12,0.68)' ,");
        out.println("                               'rgba(255,119,0,0.69)' ,");
        out.println("                               'rgba(255,0,0,0.7)' ],");
        out.println("                   map: map");
        out.println("               });");
        out.println("			}");
        out.println("");
        out.println("           function showMap()");
        out.println("           {");
        out.println("               var myForm = document.forms['newPostcode'];");
        out.println("               myForm.elements['postcode'].value = document.getElementById('pcode').value;");
        out.println("               myForm.submit();");
        out.println("           }");
        out.println("");
        out.println("           function zoomChanged()");
        out.println("           {");
        out.println("               var currentZoom = this.map.getZoom()");
        out.println("               if (currentZoom >= 18 && currentZoom <= 19)");
        out.println("                   {this.heatmap.radius = 100; clearMarkets(map); }");
        out.println("               else if (currentZoom >= 17 && currentZoom < 18)");
        out.println("                   {clearMarkets(null); MammaMia(0.5,1,90); this.heatmap.radius = 90;  }");
        out.println("               else if (currentZoom >= 16 && currentZoom < 17)");
        out.println("                   {clearMarkets(null); MammaMia(1,1,80); this.heatmap.radius = 80;  }");
        out.println("               else if (currentZoom >= 15 && currentZoom < 16)");
        out.println("                   {clearMarkets(null); MammaMia(2,1,65); this.heatmap.radius = 65;  }");
        out.println("               else if (currentZoom >= 14 && currentZoom < 15)");
        out.println("                   {clearMarkets(null); MammaMia(3,2,55); this.heatmap.radius = 55;  }");
        out.println("               else if (currentZoom >= 13 && currentZoom < 14)");
        out.println("                   {clearMarkets(null); MammaMia(4,4,45); this.heatmap.radius = 45;  }");
        out.println("               else if (currentZoom >= 12 && currentZoom < 13)");
        out.println("                   {clearMarkets(null); MammaMia(8,5,30); this.heatmap.radius = 30;  }");
        out.println("               else if (currentZoom >= 11 && currentZoom < 12)");
        out.println("                   {clearMarkets(null); MammaMia(16,10,28);this.heatmap.radius = 28;  }");
        out.println("               else if (currentZoom >= 10 && currentZoom < 11)");
        out.println("                   {clearMarkets(null); MammaMia(32,15,26); this.heatmap.radius = 26; }");
        out.println("               else if (currentZoom >= 9 && currentZoom < 10)");
        out.println("                   {clearMarkets(null); MammaMia(64,30,25); this.heatmap.radius = 25;  }");
        out.println("               else if (currentZoom >= 8 && currentZoom < 9)");
        out.println("                   {clearMarkets(null); MammaMia(128,60,20); this.heatmap.radius = 20;  }");
        out.println("               else if (currentZoom >= 7 && currentZoom < 8)");
        out.println("                   {clearMarkets(null); MammaMia(256,80,15); this.heatmap.radius = 15;  }");
        out.println("               else if (currentZoom >= 6 && currentZoom < 7)");
        out.println("                   {clearMarkets(null);  MammaMia(500,90,10); this.heatmap.radius = 10;  }");
        out.println("               else if (currentZoom >= 5 && currentZoom < 6)");
        out.println("                   {clearMarkets(null); MammaMia(800,100,5); this.heatmap.radius = 5;  }");
        out.println("           }");
        out.println("");
        out.println("			function MammaMia(radiusParam, factorParam, radHeatParam)");
        out.println("			{");
        out.println("				$.get('GetData', {typeSend:'L', latitude:map.getCenter().lat(), longitude:map.getCenter().lng, radius:radiusParam, factor:factorParam}, function(points) {");
        out.println("					var pointsArr = [];");
        out.println("					var maxVal, weightD;");
        out.println("					var maxVal = 0.0;");
        out.println("					var minVal=999999999.0;");
        out.println("					for(var i = 0; i<points.length; i++)");
        out.println("					{");
        out.println("						pointsArr.push({location: new google.maps.LatLng(points[i].latitude,points[i].longitude),weight:points[i].weight});");
        out.println("						weightD = points[i].weight;");
        out.println("						if(weightD>maxVal)");
        out.println("							{maxVal = weightD;}");
        out.println("						 if(weightD<minVal && weightD>10000)");
        out.println("							{minVal = weightD;}");
        out.println("					}");
        out.println("					initMapStrikesBack(pointsArr,radHeatParam);");
        out.println("           		document.getElementById('tabula').rows[0].cells[1].innerHTML = (maxVal/1000000).toFixed(2) + 'M';"); //" + String.format("%.2f",maxVal/1000000) + "
        out.println("           		document.getElementById('tabula').rows[1].cells[0].innerHTML = (minVal/1000000).toFixed(2) + 'M';");
        out.println("				});");
        out.println("			}");
        out.println("");
        out.println("           function clearMarkets(map)");
        out.println("           {");
        out.println("               for (var i = 0; i < markers.length; i++) {");
        out.println("               markers[i].setMap(map);}");
        out.println("           }");
        out.println("");
        out.println("           function addMarkerToMap(lat, lon, content)");
        out.println("           {");
        out.println("               var infowindow = new google.maps.InfoWindow();");
        out.println("               var myLatLng = new google.maps.LatLng(lat, lon);");
        out.println("               var marker = new google.maps.Marker");
        out.println("               ({");
        out.println("                   position: myLatLng,");
        out.println("                   map: map");
        //out.println("                     animation: google.maps.Animation.DROP,");
        out.println("               });");
        out.println("               markers.push(marker);");
        out.println("               markerCount++;");
        out.println("               google.maps.event.addListener(marker, 'click', (function(marker, markerCount)");
        out.println("               {");
        out.println("                   return function()");
        out.println("                   {");
        out.println("                       infowindow.setContent(content);");
        out.println("                       infowindow.open(map, marker);");
        out.println("                   }");
        out.println("               })(marker, markerCount));");
        out.println("           }");
        out.println("");
        // getPoints function returns an array of latitude, longitude and price weight to the HeatmapLayer in the initMap function
        out.println("           function getPoints()");
        out.println("           {");
        try
        {
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL,USER,PASS);
            stat = conn.createStatement();
            rs = stat.executeQuery(sql);
            if (!rs.next())
            {
            	// not records found
                out.println("alert('Postcode not found in the records, the closest alternative will be processed');");
                if(typeFind.equals("P") && postcode.length() > 0)
                {
                	out.println("document.getElementById('pcode').value = '" + postcode.substring(0, postcode.length()-1) + "';");
                	out.println("showMap();");
                }
                out.println("return [];");
            }
            else 
            {
            	// records found
                rs.beforeFirst();
                if (rs.next()) 
                {
                    // Initially set the Minimum and Maximum longitude and latitude to the first location selected.
                    latitudeMin = rs.getDouble("latitude");
                    latitudeMax = latitudeMin;
                    longitudeMin = rs.getDouble("longitude");
                    longitudeMax = longitudeMin;
                }
                rs.beforeFirst();
                StringBuilder mapMarkers2 = new StringBuilder();
                Locale locale = null;
                NumberFormat currencyFormatter = null;
                double minVal = 999999999;
                double maxVal = 0;
                double lat = 0;
                double lon = 0;
                double weightD = 0;
                while(rs.next()) 
                {
                    lat = rs.getDouble("latitude");
                    lon = rs.getDouble("longitude");
                    // Find the Minimum and Maximum latitude and longitude selected. This will be used to calculate the centre of the map.
                    if (lat < latitudeMin) latitudeMin = lat;
                    if (lat > latitudeMax) latitudeMax = lat;
                    if (lon < longitudeMin) longitudeMin = lon;
                    if (lon > longitudeMax) longitudeMax = lon;
                    String latitude = new Double(lat).toString();
                    String longitude = new Double(lon).toString();
                    weightD = rs.getDouble("weight");
                    String weight = String.format("%,.2f", weightD);
                    if(weightD>maxVal)
                    	maxVal = weightD;
                    if(weightD<minVal && weightD>10000)
                    	minVal = weightD;
                    if(typeFind.equals("L") || (typeFind.equals("P") && zoom == 19))
                    {
                        //mapMarkers.append("addMarkerToMap(" + latitude + ", " + longitude + ", '<p>" + weight + " &#163;</p>');\n");
                        out.println("markersGenerator.push({latitude:" + lat + ", longitude:" + lon + ", content:'<p>&#163; " + weight + "</p>'});");
                    }
                    mapMarkers2.append("        {location: new google.maps.LatLng(" + latitude + "," + longitude + "), weight: " + rs.getDouble("weight") + "},");
                }
                out.println("           document.getElementById('tabula').rows[0].cells[1].innerHTML = '" + String.format("%.2f",maxVal/1000000) + "M';");
                out.println("           document.getElementById('tabula').rows[1].cells[0].innerHTML = '" + String.format("%.2f",minVal/1000000) + "M';");
                out.println("           latitudeMin=" + latitudeMin+";");
                out.println("           latitudeMax=" + latitudeMax+";");
                out.println("           longitudeMin=" + longitudeMin+";");
                out.println("           longitudeMax=" + longitudeMax+";");
                mapMarkers2.deleteCharAt(mapMarkers2.length()-1);
                out.println("return ["+mapMarkers2.toString()+"];");
            }
        }
        catch (Exception e)
        {
            System.out.println("SQLException");
            e.printStackTrace();
        }
        out.println("           }");
        out.println("       </script>");
        out.println("       <script async defer");
        
        // google maps key
        out.println("           src='https://maps.googleapis.com/maps/api/js?key=AIzaSyC-oMaxcF64dMrmSC39uN-Tk-CPfb9KvOc&libraries=visualization&callback=initMap'>");
        out.println("       </script>");
        out.println("   </body>");
        out.println("</html>");
        out.flush();
        out.close();
    }   
    
    // Response method of page call POST
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        doGet(req, resp);
    }
}