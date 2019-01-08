package com.hcpt.multirestaurants.activity.tabs.cart;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.hcpt.multirestaurants.Debug;
import com.hcpt.multirestaurants.R;
import com.hcpt.multirestaurants.config.Constant;
import com.hcpt.multirestaurants.config.GlobalValue;
import com.hcpt.multirestaurants.config.WebServiceConfig;
import com.hcpt.multirestaurants.modelmanager.ErrorNetworkHandler;
import com.hcpt.multirestaurants.modelmanager.ModelManager;
import com.hcpt.multirestaurants.modelmanager.ModelManagerListener;
import com.hcpt.multirestaurants.object.Menu;
import com.hcpt.multirestaurants.object.Relish;
import com.hcpt.multirestaurants.object.RelishOption;
import com.hcpt.multirestaurants.object.Shop;
import com.hcpt.multirestaurants.util.CustomToast;
import com.hcpt.multirestaurants.util.DialogUtility;
import com.hcpt.multirestaurants.util.NetworkUtil;
import com.hcpt.multirestaurants.widget.AutoBgButton;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Administrator on 7/9/2016.
 */
public class DeliveryInfoActivity extends Activity implements View.OnClickListener {
    public static final String MSG_SUCESS = "success";
    private static final int PAYPAL_METHOD = 2;
    private static final int DELIVERY_METHOD = 1;
    private View view;
    private TextView btnSubmitOrder, tvTotalPrice;
    private EditText txtFullName, txtEmail, txtPhone, txtAddress;
    private AutoBgButton btnOk, btnCancel;
    private String data;
    private Spinner spinnerPayment;
    private ImageView btnBack;
    private static final String CONFIG_ENVIRONMENT = PayPalConfiguration.ENVIRONMENT_NO_NETWORK;
    private static final String CONFIG_CLIENT_ID = Constant.PAYPAL_CLIENT_APP_ID;
    private static final String CONFIG_RECEIVER_EMAIL = Constant.PAYPAL_RECEIVE_EMAIL_ID;
    private static PayPalConfiguration config = new PayPalConfiguration()
            .environment(CONFIG_ENVIRONMENT)
            .clientId(CONFIG_CLIENT_ID);
    private double priceMenu = 0;
    private double totalprice;
    private TextView txtDate, txtTime;
    private DatePicker datePicker;
    private TimePicker timePicker;
    private int mYear, mMonth, mDay, mHour, mMinute;
    private String today = "";
    private long millis;
    private TextView tvAmount;
    private int numberItem = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_oder_detail_new);
        Intent intent = getIntent();
        totalprice = intent.getDoubleExtra("TOTALPRICE", 0.0);
        initUI();
        txtFullName.setText(GlobalValue.myAccount.getFull_name());
        txtEmail.setText(GlobalValue.myAccount.getEmail());
        txtPhone.setText(GlobalValue.myAccount.getPhone());
        txtAddress.setText(GlobalValue.myAccount.getAddress());
        tvTotalPrice.setText(String.valueOf(totalprice) + "$");
        initControls();
        startPaypalService();
        final Calendar c = Calendar.getInstance();
        mHour = c.get(Calendar.HOUR_OF_DAY);
        mMinute = c.get(Calendar.MINUTE);
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);

        for (int i = 0; i < GlobalValue.menuArrayList.size(); i++) {
            numberItem += GlobalValue.menuArrayList.get(i).getOrderNumber();
        }
        if (numberItem==1){
            tvAmount.setText(String.valueOf(numberItem)+"item");
        }else {
            tvAmount.setText(String.valueOf(numberItem)+"items");
        }


    }

    private void initUI() {
//        btnOk = (AutoBgButton) findViewById(R.id.btnOk);
//        btnCancel = (AutoBgButton) findViewById(R.id.btnCancel);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        btnSubmitOrder = findViewById(R.id.btnSubmitOrder);
        txtFullName = (EditText) findViewById(R.id.txtFullName);
        txtEmail = (EditText) findViewById(R.id.txtEmail);
        txtPhone = (EditText) findViewById(R.id.txtPhone);
        txtAddress = (EditText) findViewById(R.id.txtAddress);
        spinnerPayment = (Spinner) findViewById(R.id.spPaymentMethod);
        txtDate = findViewById(R.id.txtDate);
        txtTime = findViewById(R.id.txtTime);
        tvAmount = findViewById(R.id.tvAmount);
//        btnBack = (ImageView) findViewById(R.id.btnBack);
//        btnBack.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                finish();
//            }
//        });
    }

    private void initControls() {
        btnSubmitOrder.setOnClickListener(this);
        txtDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setupDatePicker();
            }
        });
        txtTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setupTimePicker();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (GlobalValue.autoBack) {
            GlobalValue.autoBack = false;
            finish();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data1) {
        if (resultCode == Activity.RESULT_OK) {
            PaymentConfirmation confirm = data1
                    .getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
            if (confirm != null) {
                JSONObject object = confirm.toJSONObject();
                Log.i("CheckOut2Activity", "Paypal OK. Response :"
                        + object.toString());
                String transactionID = "Unknown";
                try {
                    JSONObject response = object.getJSONObject("response");
                    transactionID = response.getString("id");
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                Toast toast = Toast.makeText(this, "Success",
                        Toast.LENGTH_LONG);
                toast.setGravity(
                        Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0, 0);
                toast.show();
                sendListOrderPayPal(data, PAYPAL_METHOD, transactionID);
            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
            Log.i("paymentExample", "The user canceled.");
        } else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID) {
            Log.i("paymentExample",
                    "An invalid payment was submitted. Please see the docs.");
        }
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        if (v == btnSubmitOrder) {

            if (txtDate.getText().toString() != null && txtTime.getText().toString() != null && millis > new Date().getTime()) {
                onClickOrder();
            } else {
                Toast.makeText(this, getResources().getString(R.string.validate), Toast.LENGTH_SHORT).show();
            }
        }

//        } else if (v == btnCancel) {
//            finish();
//        }
    }

    private void onClickOrder() {
        // TODO Auto-generated method stub
        String fullName = txtFullName.getText().toString();
        String email = txtEmail.getText().toString();
        String phone = txtPhone.getText().toString();
        String address = txtAddress.getText().toString();

        if (!address.isEmpty()) {
            data = createOfferJson(GlobalValue.menuArrayList, fullName, phone, email, address);
            if (NetworkUtil.checkNetworkAvailable(DeliveryInfoActivity.this)) {
                if (spinnerPayment.getSelectedItemPosition() == 0) {
                    sendListOrder(data, DELIVERY_METHOD);
                } else if (spinnerPayment.getSelectedItemPosition() == 1) {
                    requestPaypalPayment(totalprice, "Payment", "USD");
                } else {
                    Intent intent = new Intent(this, PaymentStripeActivity.class);
                    startActivity(intent);
                }
            } else {
                Toast.makeText(DeliveryInfoActivity.this,
                        R.string.message_network_is_unavailable,
                        Toast.LENGTH_LONG).show();
            }

        } else {
            Toast.makeText(DeliveryInfoActivity.this,
                    "Please input delivery address !", Toast.LENGTH_SHORT)
                    .show();
        }
    }

    private void sendListOrderPayPal(String data, final int paymentMethod, String transactionId) {
        ModelManager.sendListOrder(DeliveryInfoActivity.this, data, paymentMethod,
                true, transactionId, String.valueOf(millis / 1000L), new ModelManagerListener() {

                    @Override
                    public void onSuccess(Object object) {
                        // TODO Auto-generated method stub
                        String strJson = (String) object;
                        try {
                            JSONObject json = new JSONObject(strJson);
                            if (json.getString(WebServiceConfig.KEY_STATUS)
                                    .equals(WebServiceConfig.KEY_STATUS_SUCCESS)) {
                                CustomToast.showCustomAlert(
                                        DeliveryInfoActivity.this,
                                        getString(
                                                R.string.message_success),
                                        Toast.LENGTH_SHORT);
                                // clear list
                                GlobalValue.arrMyMenuShop.clear();
                                // go back list shop cart
                                onBackPressed();

                            } else {
                                CustomToast.showCustomAlert(
                                        DeliveryInfoActivity.this,
                                        getString(
                                                R.string.message_order_false),
                                        Toast.LENGTH_SHORT);
                            }
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(VolleyError error) {
                        // TODO Auto-generated method stub
                        Toast.makeText(DeliveryInfoActivity.this, ErrorNetworkHandler.processError(error), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void sendListOrder(String data, final int paymentMethod) {
        Debug.e("Sendorder");
        ModelManager.sendListOrder(DeliveryInfoActivity.this, data, paymentMethod,
                true, "PAYMENT_CASH_ON_DELIVERY", String.valueOf(millis / 1000L), new ModelManagerListener() {

                    @Override
                    public void onSuccess(Object object) {
                        // TODO Auto-generated method stub
                        String strJson = (String) object;
                        Debug.e(strJson);
                        try {
                            JSONObject json = new JSONObject(strJson);
                            if (json.getString(WebServiceConfig.KEY_STATUS)
                                    .equals(WebServiceConfig.KEY_STATUS_SUCCESS)) {
                                CustomToast.showCustomAlert(
                                        DeliveryInfoActivity.this,
                                        getString(
                                                R.string.message_success),
                                        Toast.LENGTH_SHORT);
                                GlobalValue.menuArrayList.clear();
                                // go back list shop cart
                                finish();

                            } else {
                                CustomToast.showCustomAlert(
                                        DeliveryInfoActivity.this,
                                        getString(
                                                R.string.message_order_false),
                                        Toast.LENGTH_SHORT);
                            }
                        } catch (JSONException e) {
                            Debug.e(e.getMessage());
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(VolleyError error) {
                        // TODO Auto-generated method stub
                        Toast.makeText(DeliveryInfoActivity.this, ErrorNetworkHandler.processError(error), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void showBankInfo(String message) {
        DialogUtility.alert(this, "Banking Information :", Html.fromHtml(message).toString());
    }

    private String createOfferJson(ArrayList<Menu> menuArrayList, String name, String phone, String email, String address) {
        JSONObject jsonOrder = new JSONObject();
        JSONArray jsonFoods = new JSONArray();

        JSONObject jsonFood = null;
        Double price = null;
        try {

                for (Menu menu : menuArrayList) {
                    price = menu.getTotalPrices();
                    priceMenu = round(price, 2);
                    jsonFood = new JSONObject();
                    JSONArray jsonOption = new JSONArray();
                    jsonFood.put(WebServiceConfig.KEY_NOTE, menu.getAddnote());
                    jsonFood.put(WebServiceConfig.KEY_ORDER_SHOP, menu.getShopId());
                    jsonFood.put(WebServiceConfig.KEY_ORDER_FOOD, menu.getId());
                    jsonFood.put(WebServiceConfig.KEY_ORDER_NUMBER_FOOD, menu.getOrderNumber());
                    jsonFood.put(WebServiceConfig.KEY_ORDER_PRICE_FOOD, round(price, 2));
                    for (Relish relish : menu.getRelishArrayList()) {
                        if (relish.getRelishOptionChoise() != null && !relish.getRelishOptionChoise().getName().equals(Constant.NONE)) {
                            JSONObject Option = new JSONObject();
                            Option.put(WebServiceConfig.KEY_ORDER_OPTION_ID, relish.getRelishId());
                            Option.put(WebServiceConfig.KEY_ORDER_OPTION_PRICE, relish.getRelishPrice());
                            Option.put("cm_id", relish.getRelishOptionChoise().getId() + "");
                            jsonOption.put(Option);
                        }
                    }
                    jsonFood.put(WebServiceConfig.KEY_ORDER_OPTION, jsonOption);
                    jsonFoods.put(jsonFood);

            }

            jsonOrder.put(WebServiceConfig.KEY_ORDER_ACCOUT_ID, GlobalValue.myAccount.getId());
            jsonOrder.put(WebServiceConfig.KEY_ORDER_NAME, name);
            jsonOrder.put(WebServiceConfig.KEY_ORDER_PHONE, phone);
            jsonOrder.put(WebServiceConfig.KEY_ORDER_EMAIL, email);
            jsonOrder.put(WebServiceConfig.KEY_ORDER_ADDRESS, address);
            jsonOrder.put(WebServiceConfig.KEY_ORDER_ITEM, jsonFoods);

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return jsonOrder.toString();
    }

    public static double round(double number, int digit) {
        if (digit > 0) {
            int temp = 1, i;
            for (i = 0; i < digit; i++)
                temp = temp * 10;
            number = number * temp;
            number = Math.round(number);
            number = number / temp;
            return number;
        } else
            return 0.0;
    }

    private void requestPaypalPayment(double value, String content,
                                      String currency) {

        PayPalPayment thingToBuy = new PayPalPayment(new BigDecimal(value),
                currency, content, PayPalPayment.PAYMENT_INTENT_SALE);

        Intent intent = new Intent(this, PaymentActivity.class);

        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        intent.putExtra(PaymentActivity.EXTRA_PAYMENT, thingToBuy);

        startActivityForResult(intent, Constant.REQUESTCODE_CHECKOUT2ACTIVITY);
    }

    private void startPaypalService() {
        Intent intent = new Intent(this, PayPalService.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        startService(intent);
    }

    @Override
    protected void onStop() {
        stopService(new Intent(this, PayPalService.class));
        super.onStop();
    }


    public void setupDatePicker() {

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, R.style.Theme_AppCompat_Light_Dialog,
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {
                        txtDate.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);
                        today = txtDate.getText().toString() + " " + txtTime.getText().toString();
                        Log.e("HUY", today);
                        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
                        Date date = null;
                        try {
                            date = sdf.parse(today);
                            millis = date.getTime();
                            Log.e("HUY", String.valueOf(millis));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }, mYear, mMonth, mDay);
        datePickerDialog.show();
    }

    // Lắng nghe sự kiện khi TimePicker thay đổi
    public void setupTimePicker() {
        // Launch Time Picker Dialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, R.style.Theme_AppCompat_Light_Dialog,
                new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay,
                                          int minute) {
                        txtTime.setText(hourOfDay + ":" + minute);
                        today = txtDate.getText().toString() + " " + txtTime.getText().toString();
                        Log.e("HUY", today);
                        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
                        Date date = null;
                        try {
                            date = sdf.parse(today);
                            millis = date.getTime();
                            Log.e("HUY", String.valueOf(millis));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                    }
                }, mHour, mMinute, true);
        timePickerDialog.show();
    }
}
