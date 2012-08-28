package jp.co.kayo.android.droiddancermotionwriter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.EditText;

public class EditActivity extends Activity {
    private long uid;
    private CheckBox checkbox1;
    private EditText editText1;
    private EditText editText2;
    private EditText editText3;
    private EditText editText4;
    private EditText editText5;
    
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        
        checkbox1 = (CheckBox)findViewById(R.id.checkbox1);
        editText1 = (EditText)findViewById(R.id.editText1);
        editText2 = (EditText)findViewById(R.id.editText2);
        editText3 = (EditText)findViewById(R.id.editText3);
        editText4 = (EditText)findViewById(R.id.editText4);
        editText5 = (EditText)findViewById(R.id.editText5);
        
        findViewById(R.id.button1).setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        findViewById(R.id.button2).setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                Intent data = new Intent();
                data.putExtra("uid", uid);
                data.putExtra("led", checkbox1.isChecked());
                data.putExtra("armleft", Byte.parseByte(editText1.getText().toString()));
                data.putExtra("armright", Byte.parseByte(editText2.getText().toString()));
                data.putExtra("rotleft", Byte.parseByte(editText3.getText().toString()));
                data.putExtra("rotright", Byte.parseByte(editText4.getText().toString()));
                data.putExtra("time", Byte.parseByte(editText5.getText().toString()));
                setResult(100, data);
                finish();
            }
        });
        
        Intent intent = getIntent();
        if(intent != null){
            uid = intent.getLongExtra("uid", 0);
            checkbox1.setChecked(intent.getBooleanExtra("led", false));
            editText1.setText(Byte.toString(intent.getByteExtra("armleft", (byte)0)));
            editText2.setText(Byte.toString(intent.getByteExtra("armright", (byte)0)));
            editText3.setText(Byte.toString(intent.getByteExtra("rotleft", (byte)0)));
            editText4.setText(Byte.toString(intent.getByteExtra("rotright", (byte)0)));
            editText5.setText(Byte.toString(intent.getByteExtra("time", (byte)0)));
        }
        
    }

}
