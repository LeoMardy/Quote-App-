package com.leoapptechnology.quotesapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.leoapptechnology.quotesapp.databinding.ActivityMainBinding;
import com.leoapptechnology.quotesapp.databinding.RvLayoutBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    HashMap<String, String> hashMap;
    ArrayList<HashMap<String, String>> myArrayList = new ArrayList<>();
    ActivityMainBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.progressBar.setVisibility(View.VISIBLE);
        String url = "https://dummyjson.com/quotes";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                binding.progressBar.setVisibility(View.GONE);
                Log.d("serverRes", response.toString());

                try {
                    JSONArray jsonArray = response.getJSONArray("quotes");

                    for (int x=0; x<jsonArray.length(); x++){
                        JSONObject jsonObject = jsonArray.getJSONObject(x);

                        String id = jsonObject.getString("id");
                        String quote = jsonObject.getString("quote");
                        String author = jsonObject.getString("author");

                        hashMap = new HashMap<>();
                        hashMap.put("id_Key",id);
                        hashMap.put("quote_Key",quote);
                        hashMap.put("author_Key",author);
                        myArrayList.add(hashMap);

                    }

                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                MyAdapter myAdapter = new MyAdapter();
                LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
                layoutManager.setOrientation(RecyclerView.VERTICAL);
                binding.recyclerView.setLayoutManager(layoutManager);
                binding.recyclerView.setAdapter(myAdapter);
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                binding.progressBar.setVisibility(View.GONE);

                new MaterialAlertDialogBuilder(MainActivity.this)
                        .setTitle("Error!")
                        .setIcon(R.drawable.baseline_error_24)
                        .setMessage("Internet Connection Failed!")
                        .setCancelable(false)
                        .setPositiveButton("Shut Down", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                finish();
                            }
                        })
                        .show();

            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(jsonObjectRequest);

    }
    private class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder>{
        private class MyViewHolder extends RecyclerView.ViewHolder{

            RvLayoutBinding binding;
            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                binding = RvLayoutBinding.bind(itemView);
            }
        }
        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_layout,parent,false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

            HashMap<String, String> hashMap = myArrayList.get(position);

            String id_Number = hashMap.get("id_Key");
            String quote = hashMap.get("quote_Key");
            String author = hashMap.get("author_Key");

            holder.binding.id.setText(id_Number);
            holder.binding.quotesText.setText(quote);
            holder.binding.authorName.setText(author);


holder.binding.shareButton.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View view) {

       new MaterialAlertDialogBuilder(MainActivity.this)
               .setTitle("Share Quote")
               .setCancelable(false)
               .setPositiveButton("Share", new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialogInterface, int i) {

                       Intent intent = new Intent();
                       intent.setAction(Intent.ACTION_SEND);
                       intent.setType("text/plain");
                       intent.putExtra(Intent.EXTRA_TEXT,quote);
                       startActivity(intent);
                   }
               })

               .setNegativeButton("Copy", new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialogInterface, int i) {

                      ClipboardManager clipboardManager = (ClipboardManager) view.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                      ClipData clipData = ClipData.newPlainText("Copy",holder.binding.quotesText.getText().toString());
                       clipboardManager.setPrimaryClip(clipData);

                       Toast.makeText(MainActivity.this, "Copyed", Toast.LENGTH_SHORT).show();
                   }
               })

               .show();

    }
});

        }

        @Override
        public int getItemCount() {
            return myArrayList.size();
        }

    }
}