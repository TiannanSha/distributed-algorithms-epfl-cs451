package cs451.LatticeAgreement;

import cs451.NetworkGlobalInfo;
import cs451.links.Packet;
import cs451.links.PerfectLink;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * config is p, vs, ds. p <= MAX_INT, ds<=1024
 *  * p denotes the number of proposals for each process, vs denotes the maximum number of elements in a proposal, and ds denotes the maximum number of distinct elements across all proposals of all processes.
 */
public class AgreementsExecutor {

    // I know the total number of
    public static int numProposals;  //p note each agreement propose one proposal and reach one decision
    public static int maxItemOneProposal; //vs
    public static int maxDistinctItemGlobal; //ds

    BufferedReader reader;
    String configFile;

    //todo this pl should probably be passed to Multiagreement and Agreement. All sending receiving should use same pl
    // with same underneath socket .
    //PerfectLink perfectLink = NetworkGlobalInfo.perfectLink;

    MultiAgreements multiAgreements;
    ExecutorService executorService = Executors.newFixedThreadPool(1);

    public AgreementsExecutor(String configFile) {
        this.configFile = configFile;
        // read configs
        try {
            this.reader = new BufferedReader(new FileReader(configFile));
            // first line is p, vs, ds
            String line = reader.readLine();
            numProposals = Integer.parseInt(line.split(" ")[0]);
            maxItemOneProposal = Integer.parseInt(line.split(" ")[1]);
            maxDistinctItemGlobal = Integer.parseInt(line.split(" ")[2]);
            //System.out.println("numProposals = " + numProposals);
            //System.out.println("maxItemOneProposal = " + maxItemOneProposal);
            //System.out.println("maxDistinctItemGlobal = " + maxDistinctItemGlobal);

            multiAgreements = new MultiAgreements(numProposals);
            executorService.submit(this::startReceiveLoop);

        } catch (Exception e) {
            e.printStackTrace();
            e.printStackTrace(System.out);
        }
    }

    public void startReceiveLoop() {

        while (true) {
            try {
                //System.out.println("in recv loop");
                // receive packets
                Packet pkt = NetworkGlobalInfo.perfectLink.deliver();
                // process packets based on message type
                MultiAgreements.handlePacket(pkt);
            } catch (Exception e) {
                e.printStackTrace();
                e.printStackTrace(System.out);
            }

        }
    }

    public void makeProposalsAndDecisions() throws IOException {
        for (int shotId=0; shotId<numProposals; shotId++) {
            // read and parse proposals
            String line = reader.readLine();
            String[] proposalStrs = line.split(" ");
            HashSet<Integer> proposal = new HashSet<>();
            for (String str:proposalStrs) {
                proposal.add(Integer.parseInt(str));
            }
            // propose() waits for a decision to be reached for this shot ID
            HashSet<Integer> decision = multiAgreements.propose(shotId, proposal);
//            System.out.println("decision: " + decision);

            // append decision to log
            NetworkGlobalInfo.getLogger().appendDecisionLogs(decision);
        }
    }

    public void stopExecutionNow() {
        // stop thread pool
        executorService.shutdownNow();
        try {
            executorService.awaitTermination(0, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
