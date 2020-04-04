# DPS invoker

### Please do not change the [FaasInvoker](src/main/java/dps/invoker/FaaSInvoker.java) interface. It is used by the EE and other students.


---------------
## The DB consists of 4 Tables:

- **Functions** : This Table is used to store all the main and alternative function links. It is where the scheduler will check what alternatives are available for a given main implementation. You will have to add the resource links you want to this Table for the scheduler to use them when creating an alternative Strategy.

- **Invocations** : This Table is used to store the data of all past invocations. The monitored Functions invocations are saved here. When the Scheduler has gotten the list of alternatives from the "Functions" Table he will calculate each alternatives availability using the data in this ("Invocations") Table. If no data is set in the Invocations table the availability will be assumed to be 1! This table can be filled using the "DataBaseFiller" contained in the EnactmentEngine.

- **Regions** :This Table can be used if you want to set an individual availability for a region. This availability value will then be considered by the scheduler when calculating the availability of the various functions. If no availability is set for a region in the "Regions" Table it will be assumed to be 1.0 in the calculation

- **Simulated Availability** :This Table can be used to simulate failures when running a workflow. You can set a wanted simulated availability for every function in this table. The FunctionNode of the EE will then pass the wanted simulated availability as a parameter "availability". This availability parameter will make my testing functions fail with a given availability. An example of my Testing Function can be seen at "arn:aws:lambda:eu-central-1:170392512081:function:TestFunction".