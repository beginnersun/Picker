package com.hzc.widget.picker.file;

import android.Manifest;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.RecyclerView;

import com.hzc.widget.R;
import com.hzc.widget.databinding.ActivityFilePickerBinding;
import com.zch.last.activity.BaseMVVMActivity;
import com.zch.last.utils.UtilPermission;
import com.zch.last.utils.UtilThread;
import com.zch.last.utils.UtilView;
import com.zch.last.view.recycler.model.ModelChoose;

import java.io.File;
import java.util.List;

import io.reactivex.disposables.Disposable;

public class FilePickerActivity extends BaseMVVMActivity<ActivityFilePickerBinding> implements ImplFPOperate {


    private FilePickerUiParams uiParams;

    @Override
    public int initLayoutRes() {
        return R.layout.activity_file_picker;
    }

    @Override
    public void initIntent(@NonNull Intent intent) {
        uiParams = (FilePickerUiParams) intent.getSerializableExtra(FilePicker_ViewModel.EXTRA_UI_PARAMS_KEY);
        if (uiParams == null) {
            uiParams = new FilePickerUiParams();
        }
    }

    @Override
    public void initView() {
        FilePicker_ViewModel filePicker_viewModel = new FilePicker_ViewModel(this, uiParams);
        filePicker_viewModel.setRecyclerView(viewDataBinding.pickerRecycler);
        filePicker_viewModel.setFPOperateImpl(this);
        viewDataBinding.setVmFilePicker(filePicker_viewModel);
        viewDataBinding.setFpUiParams(uiParams);

        //

        switch (uiParams.getPickType()) {
            case FILE:
                viewDataBinding.pickedDesc.setHint("请选择文件");
                UtilView.setViewVisibility(viewDataBinding.pickerExtraBtn, View.GONE);
                break;
            case FILE_OR_FOLDER:
                viewDataBinding.pickedDesc.setHint("未选择文件将返回当前文件夹");
                UtilView.setViewVisibility(viewDataBinding.pickerExtraBtn, View.VISIBLE);
                viewDataBinding.pickerExtraBtn.setText("创建");
                break;
            case FOLDER:
                viewDataBinding.pickedDesc.setText("请选择文件夹");
                UtilView.setViewVisibility(viewDataBinding.pickerExtraBtn, View.VISIBLE);
                viewDataBinding.pickerExtraBtn.setText("创建");
                break;
        }

    }

    @Override
    public void initListener() {
        viewDataBinding.pickerRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    viewDataBinding.pickerSearch.clearFocus();
                }
            }
        });
        viewDataBinding.pickerPathText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                viewDataBinding.pickerPathHorScroll.smoothScrollTo(viewDataBinding.pickerPathText.getWidth(), 0);
            }
        });
        viewDataBinding.pickerSearch.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                viewDataBinding.pickerSearch.onActionViewCollapsed();
                return false;
            }
        });
        viewDataBinding.pickerSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            private Disposable queryDisposable;

            @Override
            public boolean onQueryTextSubmit(String query) {
                if (queryDisposable != null && !queryDisposable.isDisposed()) {
                    queryDisposable.dispose();
                }
                viewDataBinding.getVmFilePicker().notifyQuery(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(final String query) {
                if (queryDisposable != null && !queryDisposable.isDisposed()) {
                    queryDisposable.dispose();
                }
                queryDisposable = UtilThread.runOnUiThread(1000, new Runnable() {
                    @Override
                    public void run() {
                        viewDataBinding.getVmFilePicker().notifyQuery(query);
                    }
                });
                return false;
            }
        });
    }

    @Override
    public void initData() {
        //检擦权限
        boolean hasPermission = UtilPermission.hasPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (!hasPermission) {
            UtilPermission.request(this, 1, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    new UtilPermission.OnPermissionRequestListener() {
                        @Override
                        public void listen(int requestCode, @Nullable String[] requestPermissions,
                                           @Nullable List<String> grantedPermissions, @Nullable List<String> deniedPermissions) {
                            if (grantedPermissions != null) {
                                viewDataBinding.getVmFilePicker().refreshFolder();
                            }
                        }
                    });
        }
    }

    @Override
    public void onBackPressed() {
        viewDataBinding.getVmFilePicker().backFolder();
    }

    @Override
    public void fileSelected(@NonNull List<ModelChoose<File>> chooseList, @NonNull ModelChoose<File> modelChoose, boolean isChoose) {
        if (chooseList.size() == 0) {
            viewDataBinding.pickedDesc.setText(null);
        } else {
            viewDataBinding.pickedDesc.setText("已选择 " + chooseList.size() + " 项");
        }
    }

    @Override
    public void setPickedDesc(String text) {
        viewDataBinding.pickedDesc.setText(text);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        UtilPermission.listen(this, requestCode, permissions, grantResults);
    }
}
