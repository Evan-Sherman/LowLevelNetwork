import java.util.Arrays;

public class Router {
    public static void main(String[] args) throws InterruptedException {

        Router router = new Router();
        int offset = 0;
        while(offset < args.length){
            int netIp = Integer.parseInt(args[offset++]);
            int netHost = Integer.parseInt(args[offset++]);
            int bridgeId = Integer.parseInt(args[offset++]);
            int bridgePort = Integer.parseInt(args[offset++]);
            int ethernet = Integer.parseInt(args[offset++]);
            router.addConection(netIp, netHost, bridgeId, bridgePort, ethernet);
        }

        router.run();
        
    }

    private int[][] networkConnections;
    private Reader[] readers;
    private Writer[] writers;
    private int seqNo = 0;
    private int[][] neighbors;
    
    public Router(){
        this.networkConnections = new int[5][];
        this.neighbors = new int[5][];
    }

    public  void addConection(int netIp, int netHost, int bridgeId, int bridgePort, int ethernet){
        for(int i = 0; i < this.networkConnections.length; i++){
            if(this.networkConnections[i] == null){
                this.networkConnections[i] = new int[]{netIp, netHost, bridgeId, bridgePort, ethernet};
                return;
            }
        }
    }

    public void addNeighbor(int netIp, int netHost, int ethernet){
        this.neighbors[this.neighbors.length] = new int[]{netIp, netHost, ethernet};
    }

    public void initializeLinks(){
        readers = new Reader[this.networkConnections.length];
        writers = new Writer[this.networkConnections.length];
        for(int i = 0; i < this.networkConnections.length; i++){
            readers[i] = new Reader("fromB" + this.networkConnections[i][2] + "P" + this.networkConnections[i][3] + ".txt");
            writers[i] = new Writer("toB" + this.networkConnections[i][2] + "P" + this.networkConnections[i][3] + ".txt");
        }
    }

    private void resize(int mod){
        int[][] newArr = new int[this.networkConnections.length + mod][];
        for(int[] i : this.networkConnections){
            newArr[i.length] = i;
        }

        this.networkConnections = newArr;
    }

    @Override
    public String toString(){
        String res = "";
        for(int[] i : this.networkConnections){
            if(i != null){
                res += Arrays.toString(i) + "\n";
            }
        }
        return res;
    }

    public void run() throws InterruptedException{
        for(int i = 0; i < 10; i++){
            Thread.sleep(1000);
        }
    }

    public boolean ARPReq(int netId, int hostId){
        
        String broadcast = "ARP REQ" + Integer.toString(netId) + " " + Integer.toString(hostId);
        for(int i = 0; i < this.networkConnections.length; i++){
            String thisMsg = broadcast + " " + Integer.toString(this.networkConnections[i][0]) + " " + Integer.toString(this.networkConnections[i][1]) + " " + Integer.toString(this.networkConnections[i][4]);
            writers[i].writeFile(thisMsg);
        }
        return true;
    }

    public boolean forwardIP(String msg){
        String[] parts = msg.split(" ");
        int netId = Integer.parseInt(parts[1]);
        int hostId = Integer.parseInt(parts[2]);
        boolean doArp = false;
        for(int i = 0; i < this.networkConnections.length; i++){
            if(this.networkConnections[i][0] == netId){
                doArp = true;
            }
        }

        if(doArp){
            this.ARPReq(netId, hostId);
            return true;
        }else{
            this.broadcast(msg, netId);
        }

        return false;
        
    }


    public void broadcast(String msg, int netId){
        String bc = "BC ";
        for(int i = 0; i < this.networkConnections.length; i++){
            bc += Integer.toString(this.networkConnections[i][0]) + " " + Integer.toString(this.networkConnections[i][1]) + " ";
            bc += Integer.toString(this.seqNo) + " " + Integer.toString(netId) + " " + msg;
            writers[i].writeFile(bc);
            this.seqNo++;
        }
    }

    public void helloProtocol(){
        String msg = "HL ";
        for(int i = 0; i < this.networkConnections.length; i++){
            msg += Integer.toString(this.networkConnections[i][0]) + " " + Integer.toString(this.networkConnections[i][1]) + " ";
            msg += Integer.toString(this.networkConnections[i][4]);
            writers[i].writeFile(msg);
        }
    }
}
