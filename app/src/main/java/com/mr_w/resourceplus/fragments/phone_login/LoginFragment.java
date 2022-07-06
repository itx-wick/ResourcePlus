package com.mr_w.resourceplus.fragments.phone_login;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.hbb20.CountryCodePicker;
import com.mr_w.resourceplus.BR;
import com.mr_w.resourceplus.R;
import com.mr_w.resourceplus.activities.start_activity.StartActivity;
import com.mr_w.resourceplus.databinding.FragmentLoginBinding;
import com.mr_w.resourceplus.injections.data.DataManager;
import com.mr_w.resourceplus.injections.di.component.FragmentComponent;
import com.mr_w.resourceplus.injections.ui.base.BaseFragment;
import com.mr_w.resourceplus.model.PhoneValidateResponse;
import com.mr_w.resourceplus.model.users.Users;
import com.mr_w.resourceplus.storage.UserPreferences;
import com.mr_w.resourceplus.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

public class LoginFragment extends BaseFragment<FragmentLoginBinding, LoginViewModel> implements View.OnClickListener,
        LoginNavigator {

    private FragmentLoginBinding mBinding;
    private Drawable deleteIcon;
    private Drawable addIcon;
    private int currentImage;
    CountryCodePicker ccp, ccp2;
    String countryCode1 = "+92", countryCode2 = "+92", referralCountryCode = "+92", countryNameCode1, countryNameCode2;
    String phoneNumber, phoneNumber2;
    boolean state1 = false;
    boolean state2 = false;
    boolean state3 = false;
    AlertDialog alertDialog;
    ProgressDialog dialog;
    Users users = new Users();
    EditText mobile1, mobile2, referralMobile, fullName;
    private DataManager userPrefs;

    @Override
    public int getBindingVariable() {
        return BR.login;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_login;
    }

    @Override
    public void performDependencyInjection(FragmentComponent buildComponent) {
        buildComponent.inject(this);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel.setNavigator(this);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mBinding = getViewDataBinding();

        userPrefs = viewModel.getDataManager();
        ccp = mBinding.ccp1;
        ccp2 = mBinding.ccp2;
        mBinding.btnSubmit1.setOnClickListener(this);
        addIcon = getActivity().getResources().getDrawable(R.drawable.add_btn_layout);
        deleteIcon = getActivity().getResources().getDrawable(R.drawable.minus_btn_layout);
        mBinding.add1.setOnClickListener(this);
        mBinding.del1.setOnClickListener(this);
        currentImage = R.drawable.add_btn_layout;
        mobile1 = mBinding.mobileNumber1;
        mobile2 = mBinding.mobileNumber2;
        fullName = mBinding.fullName;
        referralMobile = mBinding.referralNumber1;


        ccp.setOnCountryChangeListener(new CountryCodePicker.OnCountryChangeListener() {
            @Override
            public void onCountrySelected() {
                countryCode1 = ccp.getSelectedCountryCodeWithPlus();
                countryNameCode1 = ccp.getSelectedCountryNameCode();
            }
        });
        ccp2.setOnCountryChangeListener(new CountryCodePicker.OnCountryChangeListener() {
            @Override
            public void onCountrySelected() {
                countryCode2 = ccp2.getSelectedCountryCodeWithPlus();
                countryNameCode2 = ccp2.getSelectedCountryNameCode();
            }
        });

        mobile1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mobile1.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().length() == 1 && s.toString().startsWith("0")) {
                    s.clear();
                }
                if (s.toString().startsWith("319")) {
                    mBinding.checkView.setText("Please Enter Valid Number 1");
                    mBinding.checkView.setVisibility(View.VISIBLE);
                    return;
                } else {
                    mBinding.checkView.setVisibility(View.GONE);
                }
                if (s.toString().isEmpty()) {
                    mBinding.checkView.setVisibility(View.GONE);
                }
                checkBoth();
            }
        });

        mobile2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mobile2.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().length() == 1 && s.toString().startsWith("0")) {
                    s.clear();
                }
                if (s.toString().startsWith("319")) {
                    mBinding.checkView.setText("Please Enter Valid Code");
                    mBinding.checkView.setVisibility(View.VISIBLE);
                } else {
                    mBinding.checkView.setVisibility(View.GONE);
                }
                if (s.toString().isEmpty()) {
                    mBinding.checkView.setVisibility(View.GONE);
                }
                checkBoth();
            }
        });

        referralMobile.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                referralMobile.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().length() == 1 && s.toString().startsWith("0")) {
                    s.clear();
                }
                if (s.toString().startsWith("319")) {
                    mBinding.checkView.setText("Please Enter Valid Code");
                    mBinding.checkView.setVisibility(View.VISIBLE);
                } else {
                    mBinding.checkView.setVisibility(View.GONE);
                }
                if (s.toString().isEmpty()) {
                    mBinding.checkView.setVisibility(View.GONE);
                }
            }
        });

        fullName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                fullName.setFilters(new InputFilter[]{new InputFilter.LengthFilter(24)});
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().isEmpty()) {
                    mBinding.checkViewFullName.setVisibility(View.GONE);
                }
            }
        });
    }

    private void checkBoth() {
        if (!mobile1.getText().toString().equals("") && !mobile2.getText().toString().equals("")) {
            if (mobile1.getText().toString().equals(mobile2.getText().toString())) {
                mBinding.checkView.setText("Both numbers are same.");
                mBinding.checkView.setVisibility(View.VISIBLE);
            } else {
                mBinding.checkView.setVisibility(View.GONE);
            }
        }
    }

    public void createDialog(View v, String mobile1, String mobile2) {
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getContext(), R.style.DialogSlideAnim));
        ViewGroup viewGroup = getActivity().findViewById(android.R.id.content);
        final View dialogView = LayoutInflater.from(v.getContext()).inflate(R.layout.select_number, viewGroup, false);
        builder.setView(dialogView);

        ImageView close = (ImageView) dialogView.findViewById(R.id.close_dialog);
        TextView textTitle = (TextView) dialogView.findViewById(R.id.dialog_title);
        ConstraintLayout mobile1Layout = (ConstraintLayout) dialogView.findViewById(R.id.female_check_layout);
        ConstraintLayout mobile2Layout = (ConstraintLayout) dialogView.findViewById(R.id.male_check_layout);

        alertDialog = builder.create();
        ((TextView) mobile1Layout.findViewById(R.id.mobileNumber1_text)).setText(mobile1);
        ((TextView) mobile2Layout.findViewById(R.id.mobileNumber2_text)).setText(mobile2);

        mobile1Layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Uncheck mobile number 2
                ((TextView) mobile2Layout.findViewById(R.id.mobileNumber2_text)).setTextColor(Color.BLACK);
                ((TextView) mobile2Layout.findViewById(R.id.mobileNumber2_text)).setText(mobile2);
                mobile2Layout.setBackgroundResource(R.drawable.custom_unchecked_layout);
                ((CheckBox) mobile2Layout.findViewById(R.id.mobileNumber2_check)).setChecked(false);
                //Check Mobile Number 1
                ((TextView) mobile1Layout.findViewById(R.id.mobileNumber1_text)).setTextColor(Color.WHITE);
                ((TextView) mobile1Layout.findViewById(R.id.mobileNumber1_text)).setText(mobile1);
                mobile1Layout.setBackgroundResource(R.drawable.custom_checked_layout);
                ((CheckBox) mobile1Layout.findViewById(R.id.mobileNumber1_check)).setChecked(true);
                phoneNumber = mobile1;
                phoneNumber2 = mobile2;
//                users.setPhone(phoneNumber);
//                users.setPhone2(phoneNumber2);
                if (!phoneNumber.isEmpty() && phoneNumber == mobile1) {
                    final Handler handler = new Handler();
                    final Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            if (alertDialog.isShowing()) {
                                state3 = true;
                                alertDialog.dismiss();
                                progressDialogShow();
                                View v = mBinding.getRoot();
                                state3(v);
                            }
                        }
                    };
                    handler.postDelayed(runnable, 1000);
                }
            }
        });

        mobile2Layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Uncheck mobile number 1
                ((TextView) mobile1Layout.findViewById(R.id.mobileNumber1_text)).setTextColor(Color.BLACK);
                ((TextView) mobile1Layout.findViewById(R.id.mobileNumber1_text)).setText(mobile1);
                mobile1Layout.setBackgroundResource(R.drawable.custom_unchecked_layout);
                ((CheckBox) mobile1Layout.findViewById(R.id.mobileNumber1_check)).setChecked(false);
                //check mobile number 2
                ((TextView) mobile2Layout.findViewById(R.id.mobileNumber2_text)).setTextColor(Color.WHITE);
                ((TextView) mobile2Layout.findViewById(R.id.mobileNumber2_text)).setText(mobile2);
                mobile2Layout.setBackgroundResource(R.drawable.custom_checked_layout);
                ((CheckBox) mobile2Layout.findViewById(R.id.mobileNumber2_check)).setChecked(true);
                phoneNumber = mobile2;
                phoneNumber2 = mobile1;
//                users.setPhone(phoneNumber);
//                users.setPhone2(phoneNumber2);
                if (!phoneNumber.isEmpty() && phoneNumber == mobile2) {
                    final Handler handler = new Handler();
                    final Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            if (alertDialog.isShowing()) {
                                state3 = true;
                                alertDialog.dismiss();
                                progressDialogShow();
                                View v = mBinding.getRoot();
                                state3(v);
                            }
                        }
                    };
                    handler.postDelayed(runnable, 1000);
                }
            }
        });

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//                alertDialog.getWindow().getAttributes().windowAnimations= R.style.DialogSlideAnim;
        alertDialog.show();
    }

    public static boolean isPhoneNumberValidate(String countryCode, String mobNumber) {
        PhoneValidateResponse phoneNumberValidate = new PhoneValidateResponse();
        PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
        Phonenumber.PhoneNumber phoneNumber = null;
        boolean finalNumber = false;
        PhoneNumberUtil.PhoneNumberType isMobile = null;
        boolean isValid = false;
        try {
            String isoCode = phoneNumberUtil.getRegionCodeForCountryCode(Integer.parseInt(countryCode));
            phoneNumber = phoneNumberUtil.parse(mobNumber, isoCode);
            isValid = phoneNumberUtil.isValidNumber(phoneNumber);
            isMobile = phoneNumberUtil.getNumberType(phoneNumber);
            phoneNumberValidate.setCode(String.valueOf(phoneNumber.getCountryCode()));
            phoneNumberValidate.setPhone(String.valueOf(phoneNumber.getNationalNumber()));
            phoneNumberValidate.setValid(false);
        } catch (NumberParseException | NullPointerException | NumberFormatException e) {
            e.printStackTrace();
        }
        if (isValid && (PhoneNumberUtil.PhoneNumberType.MOBILE == isMobile)) {
            finalNumber = true;
            phoneNumberValidate.setValid(true);
        }
        return finalNumber;
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSubmit1:
                Utils.hideKeyboard(getActivity());
                checkData(v);
                break;
            case R.id.add1:
                if (currentImage == R.drawable.add_btn_layout) {
                    mBinding.mobileNumberLayout2.setVisibility(View.VISIBLE);
                    mBinding.add1.setImageResource(R.drawable.minus_btn_layout);
                    mBinding.del1.setImageResource(R.drawable.minus_btn_layout);
                    currentImage = R.drawable.minus_btn_layout;
                } else {
                    mBinding.del1.setImageResource(R.drawable.add_btn_layout);
                    mBinding.mobileNumberLayout.setVisibility(View.GONE);
                    currentImage = R.drawable.add_btn_layout;
                    mBinding.mobileNumber1.setText("");
                }
                break;
            case R.id.del1:
                if (currentImage == R.drawable.add_btn_layout) {
                    mBinding.mobileNumberLayout.setVisibility(View.VISIBLE);
                    mBinding.add1.setImageResource(R.drawable.minus_btn_layout);
                    mBinding.del1.setImageResource(R.drawable.minus_btn_layout);
                    currentImage = R.drawable.minus_btn_layout;
                } else {
                    mBinding.add1.setImageResource(R.drawable.add_btn_layout);
                    mBinding.mobileNumberLayout2.setVisibility(View.GONE);
                    currentImage = R.drawable.add_btn_layout;
                    mBinding.mobileNumber2.setText("");
                }
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + v.getId());
        }
    }

    private void Check(View v) {

        JSONObject params = new JSONObject();
        try {
            params.put("name", userPrefs.getUserDetails().getName());
            params.put("number", userPrefs.getUserDetails().getPhoneNo());
        } catch (JSONException exception) {
            exception.printStackTrace();
        }

        viewModel.setActivity(new WeakReference<>((StartActivity) getActivity()));
        viewModel.setView(new WeakReference<>(v));
        viewModel.login(params);
    }

    private void progressDialogShow() {
        if (dialog == null) {
            dialog = new ProgressDialog(getActivity());
            dialog.show();
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.setContentView(R.layout.progress_dialog);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }

    private void state3(View v) {
        if (state2 == state3) {
            final Handler handler = new Handler();
            final Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    dialog.dismiss();
                    users.setPhoneNo(phoneNumber);
                    userPrefs.setUserDetails(users);
                    userPrefs.saveBoolean(UserPreferences.PREF_USER_IS_LOGIN, true);
                    Check(v);
                }
            };
            handler.postDelayed(runnable, 1000);
        }
    }

    private void checkData(View v) {
        if (!state1) {
            if (!mBinding.mobileNumber1.getText().toString().isEmpty() && !mBinding.mobileNumber2.getText().toString().isEmpty()) {

                if (mBinding.mobileNumber1.getText().toString().startsWith("319")) {
                    mBinding.checkView.setText("Please Enter Valid Number 1");
                    mBinding.checkView.setVisibility(View.VISIBLE);
                    return;
                } else if (isPhoneNumberValidate(countryCode1, mBinding.mobileNumber1.getText().toString())) {
                    mBinding.checkView.setVisibility(View.GONE);
                } else {
                    mBinding.checkView.setText("Please Enter Valid Number 1");
                    mBinding.checkView.setVisibility(View.VISIBLE);
                    return;
                }

                if (mBinding.mobileNumber2.getText().toString().startsWith("319")) {
                    mBinding.checkView.setText("Please Enter Valid Number 2");
                    mBinding.checkView.setVisibility(View.VISIBLE);
                    return;
                } else if (isPhoneNumberValidate(countryCode2, mBinding.mobileNumber2.getText().toString())) {
                    mBinding.checkView.setVisibility(View.GONE);
                    state2 = true;
                } else {
                    mBinding.checkView.setText("Please Enter Valid Number 2");
                    mBinding.checkView.setVisibility(View.VISIBLE);
                    return;
                }
            } else if (mBinding.mobileNumber1.getText().toString().isEmpty() && !mBinding.mobileNumber2.getText().toString().isEmpty()) {
                if (mBinding.mobileNumber2.getText().toString().isEmpty()) {
                    mBinding.checkView.setVisibility(View.VISIBLE);
                    return;
                } else if (mBinding.mobileNumber1.getText().toString().isEmpty() && !mBinding.mobileNumber2.getText().toString().isEmpty()) {
                    mBinding.checkView.setVisibility(View.GONE);
                    if (mBinding.mobileNumber2.getText().toString().startsWith("319")) {
                        mBinding.checkView.setText("Please Enter Valid Number 2");
                        mBinding.checkView.setVisibility(View.VISIBLE);
                        return;
                    } else if (isPhoneNumberValidate(countryCode2, mBinding.mobileNumber2.getText().toString())) {
                        mBinding.checkView.setVisibility(View.GONE);
                        users.setPhoneNo(countryCode2 + "" + mBinding.mobileNumber2.getText().toString());
                    } else {
                        mBinding.checkView.setText("Please Enter Valid Number 2");
                        mBinding.checkView.setVisibility(View.VISIBLE);
                        return;
                    }
                }
            } else {
                if (mBinding.mobileNumber1.getText().toString().isEmpty()) {
                    mBinding.checkView.setVisibility(View.VISIBLE);
                    return;
                } else if (!mBinding.mobileNumber1.getText().toString().isEmpty() && mBinding.mobileNumber2.getText().toString().isEmpty()) {
                    mBinding.checkView.setVisibility(View.GONE);
                    if (mBinding.mobileNumber1.getText().toString().startsWith("319")) {
                        mBinding.checkView.setText("Please Enter Valid Number 1");
                        mBinding.checkView.setVisibility(View.VISIBLE);
                        return;
                    } else if (isPhoneNumberValidate(countryCode1, mBinding.mobileNumber1.getText().toString())) {
                        mBinding.checkView.setVisibility(View.GONE);
                        users.setPhoneNo(countryCode1 + "" + mBinding.mobileNumber1.getText().toString());
//                        users.setPhone2("");
                    } else {
                        mBinding.checkView.setText("Please Enter Valid Number 1");
                        mBinding.checkView.setVisibility(View.VISIBLE);
                        return;
                    }
                }
            }
        }

        if (mBinding.fullName.getText().toString().isEmpty()) {
            mBinding.checkViewFullName.setVisibility(View.VISIBLE);
            return;
        } else {
            mBinding.checkViewFullName.setVisibility(View.GONE);
            users.setName(mBinding.fullName.getText().toString());
        }
        if (!mBinding.mobileNumber1.getText().toString().isEmpty() && !mBinding.mobileNumber2.getText().toString().isEmpty() && !mBinding.fullName.getText().toString().isEmpty()) {
            createDialog(v, countryCode1 + "" + mBinding.mobileNumber1.getText().toString(), countryCode2 + "" + mBinding.mobileNumber2.getText().toString());
        }
        if (!state2) {
            userPrefs.setUserDetails(users);
            Check(v);
        }
    }

    @Override
    public void showProgressBar() {
        progressDialogShow();
    }

    @Override
    public void hideProgressBar() {
        if (dialog.isShowing())
            dialog.dismiss();
    }
}