//package org.consensus.assignment2starterCode;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class CompliantNode implements Node {

    private Set followees;
    private double p_graph;
    private double p_malicious;
    private double p_txDistribution;
    private int numRounds;
    private Set<Transaction> proposals;

    public CompliantNode(double p_graph, double p_malicious, double p_txDistribution, int numRounds) {
        this.followees = new HashSet();
        this.proposals = null;
        this.p_graph = p_graph;
        this.p_malicious = p_malicious;
        this.p_txDistribution = p_txDistribution;
        this.numRounds = numRounds;
    }

    public void setFollowees(boolean[] followees) {

        if (followees != null)
            for (int j=0; j<followees.length; j++) {

                if (followees[j] == true)
                    this.followees.add(j);
            }

    }

    public void setPendingTransaction(Set<Transaction> pendingTransactions) {

        this.proposals = pendingTransactions;
    }

    public Set<Transaction> sendToFollowers() {

        if (this.proposals != null)
            return this.proposals;
        else
            return null;
    }

    public void receiveFromFollowees(Set<Candidate> candidates) {

        Iterator it = candidates.iterator();

        if (candidates != null)
            while (it.hasNext()) {
                Candidate cand = (Candidate)it.next();
                if (cand.tx != null)
                    this.proposals.add(cand.tx);
            }

    }
}
