package com.example.dotolist;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private EditText editTextTask;
    private ListView listViewTasks;
    private ArrayList<String> taskList;
    private ArrayAdapter<String> taskAdapter;
    private DatabaseHelper databaseHelper;
    private int selectedItemPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextTask = findViewById(R.id.editTextTask);
        listViewTasks = findViewById(R.id.listViewTasks);

        taskList = new ArrayList<>();
        taskAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, taskList);
        listViewTasks.setAdapter(taskAdapter);

        databaseHelper = new DatabaseHelper(this);

        loadTasks();

        listViewTasks.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedItemPosition = position;
                showEditTaskDialog();
            }
        });
    }

    private void showEditTaskDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit");
        builder.setMessage("Pilih aksi:");

        builder.setPositiveButton("Hapus", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteTask(selectedItemPosition);
            }
        });

        builder.setNegativeButton("Ubah Nama", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showRenameTaskDialog();
            }
        });

        builder.setNeutralButton("Batal", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showRenameTaskDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Ubah Nama Tugas");

        final EditText input = new EditText(this);
        input.setText(taskList.get(selectedItemPosition));
        builder.setView(input);

        builder.setPositiveButton("Simpan", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newName = input.getText().toString();
                if (!newName.isEmpty()) {
                    editTask(selectedItemPosition, newName);
                } else {
                    Toast.makeText(MainActivity.this, "Nama Tidak Boleh Kosong", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Batal", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void editTask(int position, String newName) {
        String oldName = taskList.get(position);

        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_TASK, newName);
        db.update(DatabaseHelper.TABLE_TASKS, values, DatabaseHelper.COLUMN_TASK + " = ?", new String[]{oldName});
        db.close();

        taskList.set(position, newName);
        taskAdapter.notifyDataSetChanged();
    }

    private void deleteTask(int position) {
        String task = taskList.get(position);

        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.delete(DatabaseHelper.TABLE_TASKS, DatabaseHelper.COLUMN_TASK + " = ?", new String[]{task});
        db.close();

        taskList.remove(position);
        taskAdapter.notifyDataSetChanged();
    }

    private void loadTasks() {
        taskList.clear();

        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + DatabaseHelper.TABLE_TASKS, null);

        if (cursor != null && cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_TASK);
            if (columnIndex == -1) {
                Toast.makeText(this, "Kolom tidak ditemukan", Toast.LENGTH_SHORT).show();
                return;
            }

            do {
                taskList.add(cursor.getString(columnIndex));
            } while (cursor.moveToNext());

            cursor.close();
        }

        db.close();

        taskAdapter.notifyDataSetChanged();
    }

    public void addTask(View view) {
        String task = editTextTask.getText().toString().trim();

        if (!task.isEmpty()) {
            SQLiteDatabase db = databaseHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_TASK, task);
            db.insert(DatabaseHelper.TABLE_TASKS, null, values);
            db.close();

            editTextTask.getText().clear();
            loadTasks();
        } else {
            Toast.makeText(this, "Data Tidak Bisa Kosong", Toast.LENGTH_SHORT).show();
        }
    }
}