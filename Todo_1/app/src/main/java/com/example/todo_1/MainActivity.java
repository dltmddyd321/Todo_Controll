package com.example.todo_1;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.todo_1.adapters.NotesAdapter;
import com.example.todo_1.database.NotesDatabase;
import com.example.todo_1.entities.Note;
import com.example.todo_1.listeners.NoteListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NoteListener {

    //메모 추가에 대한 코드 값
    public static final int REQUEST_CODE_ADD_NOTE = 1;

    //메모 갱신에 대한 코드 값
    public static final int REQUEST_CODE_UPDATE_NOTE = 2;

    //노트 시각화에 대한 코드 값
    public static final int REQUEST_CODE_SHOW_NOTES = 3;

    //이미지 선택 및 호출에 대한 코드 값
    public static final int REQUEST_CODE_SELECT_IMAGE = 4;

    //저장소 권한에 대한 코다ㅡ 값
    public static final int REQUEST_CODE_STORAGE_PERMISSION = 5;

    //노트의 목록을 확인할 수 있는 RecyclerView와 그 노트들이 담길 List 선언언
    private RecyclerView notesRecyclerView;
    private List<Note> noteList;
    private NotesAdapter notesAdapter;

    //노트 클릭 시 작용을 위한 변수
    private int noteClickedPosition = -1;

    private AlertDialog dialogAddURL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView imgAddNoteMain = findViewById(R.id.imgAddNoteMain);
        imgAddNoteMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //메인화면 하단의 노트 추가 버튼을 누르면 메모 작성 창으로 이동
                startActivityForResult(
                        new Intent(getApplicationContext(), CreateNoteActivity.class),
                        REQUEST_CODE_ADD_NOTE
                );
            }
        });
        notesRecyclerView = findViewById(R.id.notesRecyclerView);
        notesRecyclerView.setLayoutManager(
                //RecyclerView의 구조적 형태를 수직적 그리드 레이아웃으로 설정
                new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        );

        noteList = new ArrayList<>();

        //메모 요소들을 화면에 배치시키고 저장하기 위해 Adapter 연결
        notesAdapter = new NotesAdapter(noteList, this);
        notesRecyclerView.setAdapter(notesAdapter);

        //메모 데이터 값을 호출
        getNotes(REQUEST_CODE_SHOW_NOTES, false);

        EditText inputSearch = findViewById(R.id.inputSearch);
        inputSearch.addTextChangedListener(new TextWatcher() {
            //검색창에 값을 입력한다면
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //입력값 s가 변경되면 검색 Timer 취소
                notesAdapter.cancelTimer();
            }

            @Override
            public void afterTextChanged(Editable s) {
                //메모 데이터가 있다면 입력값 s에 대한 데이터를 검색
                if(noteList.size() != 0) {
                    notesAdapter.searchNotes(s.toString());
                }
            }
        });

        findViewById(R.id.imageAddNote).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //메인화면 하단의 작은 노트 추가 버튼을 누르면 메모 작성 창으로 이동
                startActivityForResult(
                        new Intent(getApplicationContext(), CreateNoteActivity.class),
                        REQUEST_CODE_ADD_NOTE
                );
            }
        });

        findViewById(R.id.imageAddImg).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //메인 하단의 이미지 버튼을 클릭하면
                if(ContextCompat.checkSelfPermission(
                        getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED) {
                    //접근 권한 허가에 대한 확인이 되지 않았다면
                    ActivityCompat.requestPermissions(
                            //저장소 접근 권한에 대한 요청문 발생
                            MainActivity.this,
                            new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},
                            REQUEST_CODE_STORAGE_PERMISSION
                    );
                } else {
                    //이미지 선택 함수 실행
                    selectImage();
                }
            }
        });

        findViewById(R.id.imageAddWeb).setOnClickListener(new View.OnClickListener() {
            //메인 하단의 URL 추가 버튼 클릭하면
            @Override
            public void onClick(View v) {
                //URL 등록 다이얼로그 팝업
                showAddURLDialog();
            }
        });
    }

    private void selectImage() {
        //갤러리 저장소를 열어 특정 아이템을 선택 가능
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if(intent.resolveActivity(getPackageManager()) != null) {
            //열린 저장소에서 선택한 이미지를 그대로 가져오기
            startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_CODE_STORAGE_PERMISSION && grantResults.length > 0) {
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //접근 권한을 허가 받는다면 이미지 선택 함수 실행
                selectImage();
            } else {
                //접근 권한을 불허가 받는다면 거부 메시지 출력
                Toast.makeText(this,"권한 거부!",Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String getPathFromUri(Uri contentUri) {
        String filePath;
        Cursor cursor = getContentResolver()
                .query(contentUri,null,null,null,null);
        if(cursor == null) {
            filePath = contentUri.getPath();
        } else {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex("_data");
            filePath = cursor.getString(index);
            cursor.close();
        }
        return filePath;
    }

    @Override
    public void onNoteClicked(Note note, int position) {
        noteClickedPosition = position;
        Intent intent = new Intent(getApplicationContext(), CreateNoteActivity.class);
        intent.putExtra("isViewOrUpdate",true);
        intent.putExtra("note",note);
        startActivityForResult(intent, REQUEST_CODE_UPDATE_NOTE);
    }

    private void getNotes(final int requestCode, final boolean isNoteDeleted) {
        @SuppressLint("StaticFieldLeak")
        class GetNotesTask extends AsyncTask<Void, Void, List<Note>> {
            @Override
            protected List<Note> doInBackground(Void... voids) {
                return NotesDatabase
                        .getDatabase(getApplicationContext())
                        .noteDao().getAllNotes();
            }

            @Override
            protected void onPostExecute(List<Note> notes) {
                super.onPostExecute(notes);
                if(requestCode == REQUEST_CODE_SHOW_NOTES) {
                    noteList.addAll(notes);
                    notesAdapter.notifyDataSetChanged();
                } else if(requestCode == REQUEST_CODE_ADD_NOTE) {
                    noteList.add(0, notes.get(0));
                    notesAdapter.notifyItemInserted(0);
                    notesRecyclerView.smoothScrollToPosition(0);
                } else if(requestCode == REQUEST_CODE_UPDATE_NOTE) {
                    noteList.remove(noteClickedPosition);

                    if(isNoteDeleted) {
                        notesAdapter.notifyItemRemoved(noteClickedPosition);
                    } else {
                        noteList.add(noteClickedPosition, notes.get(noteClickedPosition));
                        notesAdapter.notifyItemChanged(noteClickedPosition);
                    }
                }
            }
        }
        new GetNotesTask().execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE_ADD_NOTE && resultCode == RESULT_OK) {
            getNotes(REQUEST_CODE_ADD_NOTE, false);
        } else if(requestCode == REQUEST_CODE_UPDATE_NOTE && resultCode == RESULT_OK) {
            if(data != null) {
                getNotes(REQUEST_CODE_UPDATE_NOTE, data.getBooleanExtra("isNoteDeleted",false));
            }
        } else if(requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK) {
            if(data != null) {
                Uri selectedImgUri = data.getData();
                if(selectedImgUri != null) {
                    try {
                        String selectedImgPath = getPathFromUri(selectedImgUri);
                        Intent intent = new Intent(getApplicationContext(), CreateNoteActivity.class);
                        intent.putExtra("isFromQuickActions",true);
                        intent.putExtra("quickActionType","image");
                        intent.putExtra("imagePath",selectedImgPath);
                        startActivityForResult(intent, REQUEST_CODE_ADD_NOTE);
                    } catch (Exception e) {
                        Toast.makeText(this,e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    private void showAddURLDialog() {
        if(dialogAddURL == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            View view = LayoutInflater.from(this).inflate(
                    R.layout.layout_add_url,
                    (ViewGroup) findViewById(R.id.layoutAddUrlContainer)
            );
            builder.setView(view);

            dialogAddURL = builder.create();
            if(dialogAddURL.getWindow() != null) {
                dialogAddURL.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }

            final EditText inputURL = view.findViewById(R.id.inputURL);
            inputURL.requestFocus();

            view.findViewById(R.id.textAdd).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(inputURL.getText().toString().trim().isEmpty()) {
                        Toast.makeText(MainActivity.this,"Enter URL", Toast.LENGTH_SHORT).show();
                    } else if(!Patterns.WEB_URL.matcher(inputURL.getText().toString()).matches()) {
                        Toast.makeText(MainActivity.this,"Enter valid URL", Toast.LENGTH_SHORT).show();
                    } else {
                        Intent intent = new Intent(getApplicationContext(), CreateNoteActivity.class);
                        intent.putExtra("isFromQuickActions",true);
                        intent.putExtra("quickActionType","URL");
                        intent.putExtra("URL",inputURL.getText().toString());
                        startActivityForResult(intent, REQUEST_CODE_ADD_NOTE);
                        dialogAddURL.dismiss();
                    }
                }
            });
            view.findViewById(R.id.textCancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialogAddURL.dismiss();
                }
            });
        }
        dialogAddURL.show();
    }
}