import java.io.*;
import java.util.Arrays;


public class Bridge {
 
    public static void main(String[] args) throws InterruptedException, IOException {
        Bridge bridge = new Bridge();
        bridge.setId(Integer.parseInt(args[0]));
        bridge.setPort(Integer.parseInt(args[1]));

        int[] neighbors = new int[args.length - 2];
        for(int i = 0; i < neighbors.length; i++){
            neighbors[i] = Integer.parseInt(args[i + 2]);
        }
        bridge.setNeighbors(neighbors);
        
        Writer[] fromFiles = new Writer[bridge.getPort() + 1];
        Reader[] toFiles = new Reader[bridge.getPort() + 1];
        Writer[] neighborFileW = new Writer[bridge.getNeighbors().length];
        Reader[] neighborFileR = new Reader[bridge.getNeighbors().length];

        for(int i = 0; i < bridge.getPort(); i++){
            fromFiles[i] = new Writer("fromB" + bridge.getId() + "P" + (i + 1) + ".txt");
            toFiles[i] = new Reader("toB" + bridge.getId() + "P" + (i + 1) + ".txt"); 
        }

        for(int i = 0; i < bridge.getNeighbors().length; i++){
            neighborFileW[i] = new Writer("B" + bridge.getId() + "B" + bridge.getNeighbors()[i] + ".txt");
            neighborFileR[i] = new Reader("B" + bridge.getNeighbors()[i] + "B" + bridge.getId() + ".txt");
        }
        bridge.initializeLinks(fromFiles, toFiles, neighborFileR, neighborFileW);
        bridge.run();

    }

    private int bridgeId;
    private int portNum;
    private int[] neighborBridges;
    private Writer[] fromPorts;
    private Reader[] toPorts;
    private Writer[] neighborBridgeW;
    private Reader[] neighborBridgeR;
    //indicies correspond with port numbers
    private int[] ethernetAddresses;


    public Bridge(){

    }

    public void initializeLinks(Writer[] fromPorts, Reader[] toPorts, Reader[] neighborBridgeR, Writer[] neighborBridgeW){

        this.fromPorts = fromPorts;
        this.toPorts = toPorts;
        this.neighborBridgeR = neighborBridgeR;
        this.neighborBridgeW = neighborBridgeW;
        this.ethernetAddresses = new int[this.fromPorts.length];
        Arrays.fill(this.ethernetAddresses, -1);
    }

    public void setId(int id){
        this.bridgeId = id;
    }   

    public int getId(){
        return this.bridgeId;
    }

    public void setPort(int port){
        this.portNum = port;
    }

    public int getPort(){
        return this.portNum;
    }

    public void setNeighbors(int[] neighbors){
        this.neighborBridges = neighbors;
    }

    public int[] getNeighbors(){
        return this.neighborBridges;
    }

    @Override
    public String toString(){
        String res = "ID: " + this.bridgeId + ", PortNum: " + this.portNum + ", Neighbors: ";
        for(int i = 0; i < this.neighborBridges.length; i++){
            res += this.neighborBridges[i];
            if(i != this.neighborBridges.length - 1){
                res += ", ";
            }
        }
        return res;
    }

    public void run() throws InterruptedException{

        for(;;){
            for(int i = 0; i < this.neighborBridges.length; i++){
                String data = this.neighborBridgeR[i].readFile();
                if(data.equals("")){
                    continue;
                }else if(data.split(" ")[0].equals("ARP")){
                    this.handleARP(data, i);
                }
                else{
                    this.redirectData(data, i);
                }
            }

            for(int i = 0; i < this.toPorts.length && this.toPorts[i] != null; i++){
                String data = this.toPorts[i].readFile();
                if(data.equals("")){
                    continue;
                }else if(data.split(" ")[0].equals("ARP")){
                    this.handleARP(data, i);
                }else{
                    this.redirectData(data, i);
                }
            }
            Thread.sleep(1000);
        }
    }

    public void redirectData(String msg, int index){
        String[] arr = msg.split(" ");
        String src = arr[1];
        String dest = arr[0];
        String type = arr[2];
        boolean known = false;
        int knownIndex = -1;


        for(int i = 0; i < this.ethernetAddresses.length; i++){
            if(this.ethernetAddresses[i] == Integer.parseInt(dest)){
                known = true;
                knownIndex = i;
                break;
            }
        }

        this.ethernetAddresses[index] = Integer.parseInt(src);
        if(known){
            this.fromPorts[knownIndex].writeFile(msg);
        }else{
            //broadcast if not known
            for(int i = 0; i < this.fromPorts.length; i++){
                if(this.fromPorts[i] != null && i != index){
                    this.fromPorts[i].writeFile(msg);
                }
            }
            for(int i = 0; i < this.neighborBridgeW.length; i++){
                if(this.neighborBridgeW[i] != null){
                    this.neighborBridgeW[i].writeFile(msg);
                }
            }
        }   
    }


    public void handleARP(String msg, int index){
        if(msg.split(" ")[1].equals("REQ")){
            this.handleARPRequest(msg, index);
        }
        else{
            this.handleARPReply(msg, index);
        }
    }

    public void handleARPReply(String msg, int index){
        
        String[] arr = msg.split(" ");
        this.ethernetAddresses[index] = Integer.parseInt(arr[4]);

        for(int i = 0; i < this.ethernetAddresses.length; i++){
            if(this.ethernetAddresses[i] == Integer.parseInt(arr[7])){
                this.fromPorts[i].writeFile(msg);
            }
        }
    }

    public void handleARPRequest(String msg, int index){
        
        this.ethernetAddresses[index] = Integer.parseInt(msg.split(" ")[6]);

        for(int i = 0; i < this.toPorts.length; i++){
            if(this.fromPorts[i] != null && i != index){
                this.fromPorts[i].writeFile(msg);
            }
        }
    }
        
}

    





    
