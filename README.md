# java-controller-runtime

 #### Steps to run the Example Operator with Controller Locally
 
 <ol>
 <li> First Clone the repository and run the command <b>mvn clean install</b> </li>
 <li> This will create an mvn repository for your project. You
      can check that locally on below path <b>.m2/repository/io/fabric8 </b></li>
 <li> After successful compilation of java-controller-runtime, go to the example folder</li>
 <li> Before running the operator, first create CRD and CR with command <b>kubectl create -f src/main/resources</b></li>
 <li> Then, run the operator first <b>mvn clean install</b> on example operator and then target
      folder has .jar file created for it.
 <li> Go to target folder and run the application with command <br>java -jar memcached-java-operator-1.0-SNAPSHOT-jar-with-dependencies.jar</li>
 <li> You can check the number of pods running with command <b>kubectl get pods</b>
   </ol>