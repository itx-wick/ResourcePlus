package com.mr_w.resourceplus.activities.document_pick;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mr_w.resourceplus.R;
import com.mr_w.resourceplus.adapter.DocumentPickAdapter;
import com.mr_w.resourceplus.adapter.FolderListAdapter;
import com.mr_w.resourceplus.interfaces.FilterResultCallback;
import com.mr_w.resourceplus.interfaces.OnSelectStateListener;
import com.mr_w.resourceplus.utils.Constant;
import com.mr_w.resourceplus.utils.Directory;
import com.mr_w.resourceplus.utils.DividerListItemDecoration;
import com.mr_w.resourceplus.utils.DocumentFile;
import com.mr_w.resourceplus.utils.FileFilter;
import com.mr_w.resourceplus.utils.FolderListHelper;

import java.util.ArrayList;
import java.util.List;

public class DocumentPickActivity extends AppCompatActivity {
    public static final int DEFAULT_MAX_NUMBER = 9;
    public static final String SUFFIX = "Suffix";
    private int mMaxNumber;
    private int mCurrentNumber = 0;
    private RecyclerView mRecyclerView;
    private DocumentPickAdapter mAdapter;
    private ArrayList<DocumentFile> mSelectedList = new ArrayList<>();
    private List<Directory<DocumentFile>> mAll;
    private ProgressBar mProgressBar;
    private String[] mSuffix;

    private TextView tv_count;
    private TextView tv_folder;
    private LinearLayout ll_folder;
    private RelativeLayout rl_done;
    private RelativeLayout tb_pick;
    private FloatingActionButton done;

    protected FolderListHelper mFolderHelper;
    protected boolean isNeedFolderList;
    public static final String IS_NEED_FOLDER_LIST = "isNeedFolderList";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vw_activity_file_pick);

        isNeedFolderList = getIntent().getBooleanExtra(IS_NEED_FOLDER_LIST, false);
        if (isNeedFolderList) {
            mFolderHelper = new FolderListHelper();
            mFolderHelper.initFolderListView(this);
        }

        mMaxNumber = getIntent().getIntExtra(Constant.MAX_NUMBER, DEFAULT_MAX_NUMBER);
        mSuffix = getIntent().getStringArrayExtra(SUFFIX);
        initView();
        loadData();
    }

    private void initView() {
        tv_count = (TextView) findViewById(R.id.tv_count);
        tv_count.setText(mCurrentNumber + "/" + mMaxNumber);
        done = findViewById(R.id.done);
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.putParcelableArrayListExtra(Constant.RESULT_PICK_FILE, mSelectedList);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        mRecyclerView = (RecyclerView) findViewById(R.id.rv_file_pick);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.addItemDecoration(new DividerListItemDecoration(this,
                LinearLayoutManager.VERTICAL, R.drawable.vw_divider_rv_file));
        mAdapter = new DocumentPickAdapter(this, mMaxNumber);
        mRecyclerView.setAdapter(mAdapter);

        mAdapter.setOnSelectStateListener(new OnSelectStateListener<DocumentFile>() {
            @Override
            public void OnSelectStateChanged(boolean state, DocumentFile file) {
                if (state) {
                    mSelectedList.add(file);
                    mCurrentNumber++;
                } else {
                    mSelectedList.remove(file);
                    mCurrentNumber--;
                }
                if (mCurrentNumber <= 0)
                    done.setVisibility(View.GONE);
                else
                    done.setVisibility(View.VISIBLE);
                tv_count.setText(mCurrentNumber + "/" + mMaxNumber);
            }
        });

        mProgressBar = (ProgressBar) findViewById(R.id.pb_file_pick);

        rl_done = (RelativeLayout) findViewById(R.id.rl_done);
        rl_done.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.putParcelableArrayListExtra(Constant.RESULT_PICK_FILE, mSelectedList);
            setResult(RESULT_OK, intent);
            finish();
        });

        tb_pick = (RelativeLayout) findViewById(R.id.tb_pick);
        ll_folder = (LinearLayout) findViewById(R.id.ll_folder);
        if (isNeedFolderList) {
            ll_folder.setVisibility(View.VISIBLE);
            ll_folder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mFolderHelper.toggle(tb_pick);
                }
            });
            tv_folder = (TextView) findViewById(R.id.tv_folder);
            tv_folder.setText(getResources().getString(R.string.vw_all));

            mFolderHelper.setFolderListListener(new FolderListAdapter.FolderListListener() {
                @Override
                public void onFolderListClick(Directory directory) {
                    mFolderHelper.toggle(tb_pick);
                    tv_folder.setText(directory.getName());

                    if (TextUtils.isEmpty(directory.getPath())) { //All
                        refreshData(mAll);
                    } else {
                        for (Directory<DocumentFile> dir : mAll) {
                            if (dir.getPath().equals(directory.getPath())) {
                                List<Directory<DocumentFile>> list = new ArrayList<>();
                                list.add(dir);
                                refreshData(list);
                                break;
                            }
                        }
                    }
                }
            });
        }
    }

    private void loadData() {
        FileFilter.getFiles(this, directories -> {
            // Refresh folder list
            if (isNeedFolderList) {
                ArrayList<Directory> list = new ArrayList<>();
                Directory all = new Directory();
                all.setName(getResources().getString(R.string.vw_all));
                list.add(all);
                list.addAll(directories);
                mFolderHelper.fillData(list);
            }

            mAll = directories;
            refreshData(directories);
        }, mSuffix);
    }

    private void refreshData(List<Directory<DocumentFile>> directories) {
        mProgressBar.setVisibility(View.GONE);
        List<DocumentFile> list = new ArrayList<>();
        for (Directory<DocumentFile> directory : directories) {
            list.addAll(directory.getFiles());
        }

        for (DocumentFile file : mSelectedList) {
            int index = list.indexOf(file);
            if (index != -1) {
                list.get(index).setSelected(true);
            }
        }

        mAdapter.refresh(list);
    }

    public void onBackClick(View v) {
        finish();
    }
}