import java.util.Arrays;

public class Host{

    public static void main(String[] args) throws InterruptedException {
    
        Host host = new Host();
        host.setIp(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
        host.setEthernet(Integer.parseInt(args[2]));
        host.setRouter(Integer.parseInt(args[3]), Integer.parseInt(args[4]));
        host.setBridge(Integer.parseInt(args[5]), Integer.parseInt(args[6]));
        host.setDest(Integer.parseInt(args[7]), Integer.parseInt(args[8]));
        try{
            host.setMessage(args[9]);
        }
        catch(ArrayIndexOutOfBoundsException e){
            host.setMessage("");
        }
        host.run();
        
    }


    private int ipNetwork;
    private int ipHost;
    private int ethernet;
    private int routerNetwork;
    private int routerHost;
    private int bridgeId;
    private int bridgePort;
    private int destNetwork;
    private int destHost;
    private String sendData;
    private String received;
    private int secSinceDataSent = -1;
    private Reader reader;
    private Writer writer;
    private Writer me;
    private String[] dataBuffer = null;
    private int destEthernet = -1;
    private int dataBufferLength = 0;
    private int[] receivedInfo = new int[3]; //[0] = ethernet, [1] = network, [2] = host
    private boolean hasBeenAcked = true;
    private int seqNo = 0;
    private int channel = 0;
    private String lastMessage = "";


    public Host(){
    }

    public void setIp(int net, int host){
        this.ipHost = host;
        this.ipNetwork = net;
        this.me = new Writer("host" + this.ipNetwork + this.ipHost + ".txt");
    }

    public void setRouter(int net, int host){
        this.routerHost = host;
        this.routerNetwork = net;
    }

    public void setBridge(int id, int port){
        this.bridgeId = id;
        this.bridgePort = port;
        this.reader = new Reader("fromB" + this.bridgeId + "P" + this.bridgePort + ".txt");
        this.writer = new Writer("toB" + this.bridgeId + "P" + this.bridgePort + ".txt");
    }

    public void setDest(int net, int host){
        this.destNetwork = net;
        this.destHost = host;
    }

    public void setMessage(String data){
        this.sendData = data;
    }

    public void receive(String data){
        this.received = data;
    }

    public void setEthernet(int id){
        this.ethernet = id;
    }

    @Override
    public String toString(){
        String res = "";
        res = "(" + this.ipNetwork + ", " + this.ipHost + ") or " + this.ethernet;
        res += " -> (" + this.routerNetwork + ", " + this.routerHost + ")";
        res += " -> (" + this.bridgeId + ", " + this.bridgePort + ")";
        res += " -> (" + this.destNetwork + ", " + this.destHost + ")";
        res += " -> " + this.sendData;

        return res;
    }

    public void run() throws InterruptedException{
        
        for(;;){
            
            this.transportTasks();
            this.ethernetReceiveFromBridge();
            Thread.sleep(1000);
            if(this.secSinceDataSent > 0)
                this.secSinceDataSent++;
        }   
    }

    //TRANSPORT LAYER
    public String[] parseData(String msg){
        String[] arr = new String[(int)(((double)msg.length() / 5.0) + 0.5)];
        for(int i = 0; i < arr.length; i++){
            try{
                arr[i] = msg.substring(i * 5, (i + 1) * 5);

            }catch(IndexOutOfBoundsException e){
                arr[i] = msg.substring(i * 5);
            }
        }

        return arr;
    }

    public void genAck(int seqNo, int channel){
        String ack = Integer.toString(this.receivedInfo[0]) + " " + Integer.toString(this.ethernet) + " ";
        ack += "IP " + "IP " + Integer.toString(this.receivedInfo[1]) + " " + Integer.toString(this.receivedInfo[2]) + " ";
        ack += Integer.toString(this.ipNetwork) + " " + Integer.toString(this.ipHost) + " ";
        ack += "ACK " + Integer.toString(seqNo) + " " + Integer.toString(channel) + " ";
        this.writer.writeFile(ack);
    }

    public void transportTasks(){
        if(!this.sendData.equals("")){
            this.dataBuffer = parseData(this.sendData);
            this.dataBufferLength = this.dataBuffer.length;
            this.sendData = "";
            this.secSinceDataSent = 0;
        }

        if(this.destEthernet == -1){
            if(this.secSinceDataSent == 0 || this.secSinceDataSent >= 30){
                this.ARPReq();
                this.secSinceDataSent = 1;
            }
            return;
        }

        if(!this.hasBeenAcked && this.secSinceDataSent == 30){
            this.writer.writeFile(this.lastMessage);
            this.secSinceDataSent = 0;
            return;
        }

        if(this.dataBufferLength == 0 || !this.hasBeenAcked){
            return;
        }

        String msg = "DA " + Integer.toString(this.seqNo) + " " +  Integer.toString(this.channel) + " " + this.dataBuffer[0];
        
        for(int i = 0; i < this.dataBuffer.length - 1; i++){
            this.dataBuffer[i] = this.dataBuffer[i + 1];
        }
        this.dataBufferLength--;
        this.ipReceiverFromTransport(msg);
    }

    public void transportReceiveFromIP(String msg){
        String[] arr = msg.split(" ");
        this.receivedInfo[1] = Integer.parseInt(arr[1]);
        this.receivedInfo[2] = Integer.parseInt(arr[2]);
        String type = arr[5];
        String data = "";
        for(int i = 5; i < arr.length; i++){
            data += arr[i] + " ";
        }
        
        me.writeFile(data);
        if(type.equals("DA")){
            this.genAck(Integer.parseInt(arr[6]), Integer.parseInt(arr[7]));
        }
        if(type.equals("ACK")){
            this.hasBeenAcked = true;
        }
    }

    //IP LAYER
    public void ipReceiveFromEthernet(){
        String[] splitMsg = this.received.split(" ");
        this.receivedInfo[0] = Integer.parseInt(splitMsg[1]);
        String msg = "";
        for(int i = 3; i < splitMsg.length; i++){
            msg += splitMsg[i] + " ";
        }
        transportReceiveFromIP(msg);
    }

    public void ipReceiverFromTransport(String msg){
        String ipMsg = "IP " + Integer.toString(this.destNetwork) + " " + Integer.toString(this.destHost) + " ";
        ipMsg += this.ipNetwork + " " + this.ipHost + " " + msg;
        this.ethernetReceiverFromIP(ipMsg);
    }


    //ETHERNET LAYER
    public boolean ethernetReceiveFromBridge(){
        String msg = this.reader.readFile();
        if(msg.equals("")){
            return false;
        }

        if(msg.split(" ")[0].equals("ARP")){
            this.handleARP(msg);
        }
        else{
            this.receive(msg);
            ipReceiveFromEthernet();

        }

        
        return true;
    }

    public void ethernetReceiverFromIP(String msg){
        String newMsg = Integer.toString(this.destEthernet) + " " + Integer.toString(this.ethernet) + " IP " + msg;

        this.writer.writeFile(newMsg);
        this.seqNo++;
        this.hasBeenAcked = false;
        this.lastMessage = newMsg;
        this.secSinceDataSent = 0;
    }

    public void ARPReq(){
        String msg = "ARP REQ " + Integer.toString(this.destNetwork) + " " + Integer.toString(this.destHost) + " ";
        msg += Integer.toString(this.ipNetwork) + " " + Integer.toString(this.ipHost) + " " + this.ethernet;
        this.writer.writeFile(msg);
    }
    
    public void ARPReply(String msg){
        String[] arr = msg.split(" ");
        String rep = "ARP REP " + Integer.toString(this.ipNetwork) + " " + Integer.toString(this.ipHost) + " " + this.ethernet + " ";
        rep += arr[4] + " " + arr[5] + " " + arr[6];
        writer.writeFile(rep);
    }

    public void handleARP(String msg){
        String[] arr = msg.split(" ");
        if(arr[1].equals("REQ") && Integer.parseInt(arr[2]) == this.ipNetwork && Integer.parseInt(arr[3]) == this.ipHost){
            this.ARPReply(msg);
            return;
        }
        else{
            this.destEthernet = Integer.parseInt(arr[4]);

        }


    }
    

}