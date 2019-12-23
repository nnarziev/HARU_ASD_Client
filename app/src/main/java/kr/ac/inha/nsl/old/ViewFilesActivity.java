package kr.ac.inha.nsl.old;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import kr.ac.inha.nsl.R;

public class ViewFilesActivity extends AppCompatActivity {

    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_files);

        listView = findViewById(R.id.list);

        List<String> values = new ArrayList<>();

        File[] allFiles = getFilesDir().listFiles();
        if (allFiles.length > 0) {
            for (File f : allFiles) {
                int file_size = Integer.parseInt(String.valueOf(f.length()));
                values.add(f.getName() + "->" + String.valueOf(file_size) + " b");
            }
        } else {
            Toast.makeText(getApplicationContext(), "No files yet!", Toast.LENGTH_SHORT).show();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, android.R.id.text1, values);
        listView.setAdapter(adapter);

    }
}
