import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
 
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
 
/**
 * TestRobot interfaces to the (real or virtual) robot over a network connection.
 * It uses Java -> JSON -> HttpRequest -> Network -> DssHost32 -> Lokarria(Robulab) -> Core -> MRDS4
 *
 * @author thomasj
 */
public class TestRobot2
{
        private Position[] path;
        private ObjectMapper mapper;
        private int nPoints;
       
   private RobotCommunication robotcomm;  // communication drivers
 
   /**
    * Create a robot connected to host "host" at port "port"
    * @param host normally http://127.0.0.1
    * @param port normally 50000
    */
   public TestRobot2(String host, int port)
   {
      robotcomm = new RobotCommunication(host, port);
   }
 
   /**
    * This simple main program creates a robot, sets up some speed and turning rate and
    * then displays angle and position for 16 seconds.
    * @param args         not used
    * @throws Exception   not caught
    */
   public static void main(String[] args) throws Exception
   {    
      System.out.println("Creating Robot");
      TestRobot2 robot = new TestRobot2("http://127.0.0.1", 50000);
     
      robot.run();
   }
 
 
   private void run() throws Exception
   {
          readPath();
          printPath();
         
      System.out.println("Creating response");
      LocalizationResponse lr = new LocalizationResponse();
 
      System.out.println("Creating request");
      DifferentialDriveRequest dr = new DifferentialDriveRequest();
     
     
      betaMove(lr, dr);
     
     
     
      // set up the request to move in a circle
      dr.setAngularSpeed(Math.PI * 0);
      dr.setLinearSpeed(1.0);
 
      System.out.println("Start to move robot");
      int rc = robotcomm.putRequest(dr);
      System.out.println("Response code " + rc);
 
      for (int i = 0; i < 250; i++)
      {
         try
         {
            Thread.sleep(1000);
         }
         catch (InterruptedException ex) {}
 
         // ask the robot about its position and angle
         robotcomm.getResponse(lr);
 
         //double angle = getBearingAngle(lr);
         double angle = lr.getHeadingAngle();
         System.out.println("bearing = " + angle * 180 / 3.1415926);
 
         double [] position = getPosition(lr);
         System.out.println("position = " + position[0] + ", " + position[1]);
      }
 
      // set up request to stop the robot
      dr.setLinearSpeed(0);
      dr.setAngularSpeed(0);
 
      System.out.println("Stop robot");
      rc = robotcomm.putRequest(dr);
      System.out.println("Response code " + rc);
 
   }
 
   /**
    * Extract the robot bearing from the response
    * @param lr
    * @return angle in degrees
    */
   double getBearingAngle(LocalizationResponse lr)
   {
      double e[] = lr.getOrientation();
 
      double angle = 2 * Math.atan2(e[3], e[0]);
      return angle * 180 / Math.PI;
   }
 
   /**
    * Extract the position
    * @param lr
    * @return coordinates
    */
   double[] getPosition(LocalizationResponse lr)
   {
      return lr.getPosition();
   }
 
 
 
   
 
        private void readPath() throws JsonParseException, JsonMappingException, IOException    {
 
                File pathFile = new File("Path-around-table-and-back.json");
                BufferedReader in = new BufferedReader(new InputStreamReader(
                new FileInputStream(pathFile)));
                mapper = new ObjectMapper();
               
                // read the path from the file
                Collection <Map<String, Object>> data = (Collection<Map<String, Object>>) mapper.readValue(in, Collection.class);
                nPoints = data.size();
                path = new Position[nPoints];
                int index = 0;
                for (Map<String, Object> point : data)  {
                        Map<String, Object> pose = (Map<String, Object>)point.get("Pose");
                        Map<String, Object> aPosition = (Map<String, Object>)pose.get("Position");
                        double x = (Double)aPosition.get("X");
                        double y = (Double)aPosition.get("Y");
                        path[index] = new Position(x, y);
                        index++;
                }
        }
       
        private void printPath(){
               
                for(int i = 0; i < nPoints; i++){
                        System.out.println("X: " + path[i].getX()+ " Y: " +path[i].getY());
                }
        }
       
        private void betaMove(LocalizationResponse lr, DifferentialDriveRequest dr)     {
                lr.getPosition();
               
               
               
               
       
        }
       
        }
 