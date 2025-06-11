package org.example.component;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;

public class DeleteButtonEditor extends AbstractCellEditor implements TableCellEditor { //刪除按鈕實作
    private final JButton button = new JButton("刪除");
    private JTable table;
    private int row;
    private final DeleteAction deleteAction;
    private final Runnable reloadCallback;

    public DeleteButtonEditor(DeleteAction deleteAction, Runnable reloadCallback) { //刪除詢問
        this.deleteAction = deleteAction;
        this.reloadCallback = reloadCallback;

        button.addActionListener(e -> {
            if (table != null && row >= 0) {
                int id = (int) table.getValueAt(row, 0);
                int confirm = JOptionPane.showConfirmDialog(null, "確定要刪除這筆記錄？", "刪除確認", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    deleteAction.delete(id);
                    JOptionPane.showMessageDialog(null, "刪除成功！");
                    SwingUtilities.invokeLater(() -> {
                        if (reloadCallback != null) reloadCallback.run();
                        fireEditingStopped();
                    });
                } else {
                    fireEditingStopped();
                }
            }
        });
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        this.table = table;
        this.row = row;
        return button;
    }

    @Override
    public Object getCellEditorValue() {
        return "刪除";
    }
}
