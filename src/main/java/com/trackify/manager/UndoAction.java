package com.trackify.manager;

import com.trackify.dao.TransactionDAO;
import com.trackify.model.Transaction;

public abstract class UndoAction {
    protected final Transaction data;

    protected UndoAction(Transaction data) {
        this.data = data;
    }

    public abstract void undo(TransactionDAO dao, int userId);

    // Text shown in the "Confirm Undo" popup
    public abstract String describe();

    public int getTransactionId() {
        return data.getId();
    }

    public static class Add extends UndoAction {
        public Add(Transaction t) {
            super(t);
        }

        @Override
        public void undo(TransactionDAO dao, int userId) {
            dao.deleteTransaction(data.getId(), userId);
        }

        @Override
        public String describe() {
            return "Undo the last ADD?\nThis will remove \"" + data.getDescription() + "\".";
        }
    }

    public static class Edit extends UndoAction {
        public Edit(Transaction oldValue) {
            super(oldValue);
        }

        @Override
        public void undo(TransactionDAO dao, int userId) {
            dao.updateTransaction(data);
        }

        @Override
        public String describe() {
            return "Undo the last EDIT?\nThis will restore \"" + data.getDescription()
                    + "\" to its previous values.";
        }
    }

    public static class Delete extends UndoAction {
        public Delete(Transaction deleted) {
            super(deleted);
        }

        @Override
        public void undo(TransactionDAO dao, int userId) {
            dao.restoreTransaction(data);
        }

        @Override
        public String describe() {
            return "Undo the last DELETE?\nThis will bring back \"" + data.getDescription() + "\".";
        }
    }
}