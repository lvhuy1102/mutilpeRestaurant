package com.hcpt.multirestaurants.activity.tabs.cart;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.hcpt.multirestaurants.BaseFragment;
import com.hcpt.multirestaurants.Debug;
import com.hcpt.multirestaurants.R;
import com.hcpt.multirestaurants.activity.MyMenuActivity;
import com.hcpt.multirestaurants.activity.tabs.MainCartActivity;
import com.hcpt.multirestaurants.activity.tabs.MainTabActivity;
import com.hcpt.multirestaurants.adapter.AdapterFoodNew;
import com.hcpt.multirestaurants.adapter.ShopCartAdapter;
import com.hcpt.multirestaurants.adapter.ShopCartAdapter.ShopCartListener;
import com.hcpt.multirestaurants.adapter.ShopCartAdapterItemNew;
import com.hcpt.multirestaurants.config.GlobalValue;
import com.hcpt.multirestaurants.object.Menu;
import com.hcpt.multirestaurants.object.Shop;
import com.hcpt.multirestaurants.util.CustomToast;

import java.util.ArrayList;

@SuppressLint("NewApi")
public class ShopCartFragment extends BaseFragment implements OnClickListener, ShopCartAdapterItemNew.deleteItem {

    private TextView btnOrder;
    private TextView lblSum, lblTotalItem;
    private RecyclerView lsvShops;
    private MainCartActivity self;
    private ArrayList<Menu> menuArrayList = new ArrayList<>();
    private double total;
    private ShopCartAdapterItemNew shopCartAdapterItemNew;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_list_shop_cart, container,
                false);
        self = (MainCartActivity) getActivity();
        initUI(view);
//        initData();
        Debug.e("shop cart");
        return view;
    }

    private void initUI(View view) {
        btnOrder = (TextView) view.findViewById(R.id.btnOrder);
        lblSum = (TextView) view.findViewById(R.id.lblSum);
        lsvShops = view.findViewById(R.id.lsvShop);
        lblTotalItem = (TextView) view.findViewById(R.id.lblTotalItem);
        btnOrder.setOnClickListener(this);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        // TODO Auto-generated method stub
        super.onHiddenChanged(hidden);
        if (!hidden) {
            refreshContent();
        }
    }


    @Override
    public void refreshContent() {
        double totalPrice = 0;
        int quality = 0;

        for (int i = 0; i < GlobalValue.menuArrayList.size(); i++) {
            Menu menu = GlobalValue.menuArrayList.get(i);
            totalPrice += menu.getTotalPrices();
            quality += menu.getOrderNumber();
        }

        total = totalPrice;
        lblSum.setText(getString(R.string.currency)
                + String.format("%.1f", totalPrice));
        lblTotalItem.setText(quality + " " + "items");
        if (menuArrayList != null) {
            menuArrayList.clear();
        }
        menuArrayList.addAll(GlobalValue.menuArrayList);
        shopCartAdapterItemNew = new ShopCartAdapterItemNew(self, menuArrayList, this);
        lsvShops.setLayoutManager(new LinearLayoutManager(self, LinearLayoutManager.VERTICAL, false));
        lsvShops.setAdapter(shopCartAdapterItemNew);
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        if (v == btnOrder) {
            onBtnOrderClick();
        }
    }

    private void onBtnOrderClick() {
        if (GlobalValue.myAccount == null) {
            CustomToast.showCustomAlert(self,
                    self.getString(R.string.message_no_login),
                    Toast.LENGTH_SHORT);
        } else if (GlobalValue.menuArrayList.size() > 0) {
            GlobalValue.autoBack = false;
            Intent intent = new Intent(getActivity(), DeliveryInfoActivity.class);
            intent.putExtra("TOTALPRICE", total);
            startActivity(intent);
        } else {
            CustomToast.showCustomAlert(self,
                    self.getString(R.string.message_no_item_menu),
                    Toast.LENGTH_SHORT);
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        refreshContent();
        shopCartAdapterItemNew.notifyDataSetChanged();
    }


    @Override
    public void deteteItem(Menu menu) {
        GlobalValue.menuArrayList.remove(menu);
        refreshContent();
    }
}
