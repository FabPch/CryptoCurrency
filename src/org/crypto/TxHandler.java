package org.crypto;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

public class TxHandler {

    private UTXOPool utxoPool;

    public TxHandler(UTXOPool utxoPool) {

        this.utxoPool = new UTXOPool(utxoPool);
    }

    public boolean isValidTx(Transaction tx) {

        double sumOutput = 0d;
        double sumInput = 0d;
        List<UTXO> verifUtxo = new ArrayList<UTXO>();

        for (int i=0; i<tx.numOutputs(); i++) { //(4), (5)

            if (tx.getOutput(i).value <= 0) {
                return false;
            } else if (tx.getOutput(i) == null) {
                return false;
            }
            sumOutput += tx.getOutput(i).value;
            UTXO toAdd = new UTXO(tx.getHash(), i);
            if (!utxoPool.contains(toAdd))
                utxoPool.addUTXO(toAdd, tx.getOutput(i));
        }

        for (int i=0; i<tx.numInputs(); i++) {

            UTXO outputToVerify = new UTXO(tx.getInput(i).prevTxHash, tx.getInput(i).outputIndex);
            if (outputToVerify != null) {

                if (!this.utxoPool.contains(outputToVerify))
                    return false;
                else if (verifUtxo.isEmpty())
                    verifUtxo.add(outputToVerify);
                else
                    for (int j=0; j<verifUtxo.size(); j++) {
                        if (outputToVerify.equals(verifUtxo.get(j)))
                            return false;
                    }
                verifUtxo.add(outputToVerify);

                byte[] dataToSign = tx.getRawDataToSign(i);
                PublicKey prevPubKey = this.utxoPool.getTxOutput(outputToVerify).address;

                if (!Crypto.verifySignature(prevPubKey, dataToSign ,tx.getInput(i).signature))
                    return false;

                if (utxoPool.getTxOutput(outputToVerify).value >= 0)
                    sumInput += utxoPool.getTxOutput(outputToVerify).value;
                else
                    return false;
            } else {
                return false;
            }

            outputToVerify = null;

        }

        if (sumOutput > sumInput)
            return false;

        return true;

    }


    public Transaction[] handleTxs(Transaction[] possibleTxs) {

        Transaction[] validTxsTab = null;

        if (possibleTxs.length <= 100 && possibleTxs.length > 0) {
            validTxsTab = new Transaction[possibleTxs.length];
        } else if (possibleTxs.length > 100){
            validTxsTab = new Transaction[100];
            for (int i=100; i<possibleTxs.length; i++) {

                for (int j=0; j<possibleTxs[i].numOutputs(); j++) {

                    UTXO toAddUtxo = null;
                    Transaction.Output toAddOutput = null;
                    //if (validTxsTab[i].getOutput(j) != null)
                        toAddUtxo = new UTXO(possibleTxs[i].getHash(), j);
                        toAddOutput = possibleTxs[i].getOutput(j);
                    if (!utxoPool.contains(toAddUtxo))
                        utxoPool.addUTXO(toAddUtxo, toAddOutput);
                }
            }
        }

        if (validTxsTab != null) {

            for (int i=0; i<validTxsTab.length; i++) {

                if (isValidTx(possibleTxs[i])) {

                    validTxsTab[i] = possibleTxs[i];
                }

            }
        }

        if (validTxsTab != null && possibleTxs != null) {

            for (int i=0; i<validTxsTab.length; i++) {

                if (validTxsTab[i] != null) {
                    //if (validTxsTab[i].numInputs() > 0) {

                        for (int j=0; j<validTxsTab[i].numInputs(); j++) {

                            UTXO toRemoveUtxo = null;
                            //if (validTxsTab[i].getInput(j) != null)
                                toRemoveUtxo = new UTXO(validTxsTab[i].getHash(), validTxsTab[i].getInput(j).outputIndex);
                                if (utxoPool.contains(toRemoveUtxo))
                                    utxoPool.removeUTXO(toRemoveUtxo);
                        }
                   // }
                }
            }
        }



        return validTxsTab;

    }


}
