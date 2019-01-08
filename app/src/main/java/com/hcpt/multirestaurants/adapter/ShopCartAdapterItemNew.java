package com.hcpt.multirestaurants.adapter;

import android.app.Dialog;
import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.hcpt.multirestaurants.R;
import com.hcpt.multirestaurants.config.Constant;
import com.hcpt.multirestaurants.object.Menu;
import com.hcpt.multirestaurants.object.Relish;
import java.util.ArrayList;


public class ShopCartAdapterItemNew extends RecyclerView.Adapter<ShopCartAdapterItemNew.ViewHolder> implements AdapterOptionTopping.OnCheckListerner, AdapterOptionTopping.OnSetDataListerner {
    private Context context;
    private ArrayList<Menu> menuArrayList;
    private Dialog dialogListToppings;
    private Dialog mDialog;
    private AdapterOptionTopping adapterOptionTopping;
    private deleteItem deleteItem;

    public ShopCartAdapterItemNew(Context context, ArrayList<Menu> menuArrayList, deleteItem deleteItem) {
        this.context = context;
        this.menuArrayList = menuArrayList;
        this.deleteItem = deleteItem;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_product_cart_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        Menu food = menuArrayList.get(position);
        Glide.with(context).load(menuArrayList.get(position).getImage()).into(holder.imgFood);
        holder.tvName.setText(menuArrayList.get(position).getName());
//
        holder.tvCategory.setText(menuArrayList.get(position).getCategoryProduct());
        holder.tvItemNumber.setText(menuArrayList.get(position).getOrderNumber() + "");
        double price = 0;
        StringBuilder listTopping = new StringBuilder();
        for (int i = 0; i < menuArrayList.get(position).getRelishArrayList().size(); i++) {
            if (food.getRelishArrayList().get(i).getRelishOptionChoise() != null && !food.getRelishArrayList().get(i).getRelishOptionChoise().getName().equals(Constant.NONE)) {
                price = price + food.getRelishArrayList().get(i).getRelishPrice();
                listTopping.append(",").append(food.getRelishArrayList().get(i).getRelishName() + ":\t" + food.getRelishArrayList().get(i).getRelishOptionChoise().getName());
            }
        }
        if (!listTopping.equals("")) {
            if (listTopping.toString().startsWith(",")) {
                String s = listTopping.substring(1, listTopping.length());
                holder.tvTopping.setText(s);
            }
        }
        holder.tvPrice.setText("$" + food.getPrice() + "+" + "(" + "$" +
                ((food.getPrice() + price) * food.getOrderNumber() - food.getPrice()) + ")");

        holder.btnAddTopping.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialogListTopping(menuArrayList.get(position));
            }
        });

        holder.btnAddIntroduction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInstructionDialog(menuArrayList.get(position));
            }
        });

        holder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                menuArrayList.remove(menuArrayList.get(position));
                deleteItem.deteteItem(menuArrayList.get(position));
                notifyDataSetChanged();
            }
        });
        holder.tvItemNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showListQuantities(menuArrayList.get(position));
            }
        });

    }

    @Override
    public int getItemCount() {
        return menuArrayList.size();
    }

    @Override
    public void onCheckChange(boolean isChecked, int positionp) {


    }

    @Override
    public void onCheckChange(Relish relish, int positionp, int p) {


    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView imgFood;
        private TextView tvName, tvCategory, tvPrice, tvItemNumber, btnAddTopping, btnAddIntroduction;
        private TextView tvTopping;
        private Button btnDelete;

        public ViewHolder(View itemView) {
            super(itemView);
            imgFood = itemView.findViewById(R.id.imgFood);
            tvName = itemView.findViewById(R.id.tvName);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvPrice = itemView.findViewById(R.id.tvTotalPrice);
            tvTopping = itemView.findViewById(R.id.tvTopping);
            tvItemNumber = itemView.findViewById(R.id.tvItemNumber);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnAddTopping = itemView.findViewById(R.id.btnAddTopping);
            btnAddIntroduction = itemView.findViewById(R.id.btnAddIntroduction);
        }
    }


    private void showInstructionDialog(final Menu food) {
        mDialog = new Dialog(context, R.style.AppTheme_OrderDetailsDialog);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setContentView(R.layout.dialog_instruction);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(mDialog.getWindow().getAttributes());
        lp.width = 4 * (context.getResources().getDisplayMetrics().widthPixels / 5);
        mDialog.getWindow().setAttributes(lp);
        mDialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_dialog);
        mDialog.setCancelable(false);

        // Get view
        final EditText edtInsTruction = (EditText) mDialog.findViewById(R.id.edtInsTructions);
        TextView btnCancel = (TextView) mDialog.findViewById(R.id.btnCancel);
        TextView btnSave = (TextView) mDialog.findViewById(R.id.btnSave);

        btnCancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mDialog.dismiss();
            }
        });
        btnSave.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (edtInsTruction.getText().toString().length() > 0) {
                    food.setAddnote(edtInsTruction.getText().toString());
                }
                mDialog.dismiss();
            }
        });
        mDialog.show();
    }

    private void showDialogListTopping(Menu menu) {
        dialogListToppings = new Dialog(context, R.style.AppTheme_OrderDetailsDialog);
        dialogListToppings.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogListToppings.setContentView(R.layout.dialog_option);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialogListToppings.getWindow().getAttributes());
        lp.width = 19 * (context.getResources().getDisplayMetrics().widthPixels / 20);
//        lp.height = 3 * (displaymetrics.heightPixels / 5);
        dialogListToppings.getWindow().setAttributes(lp);
        dialogListToppings.getWindow().setBackgroundDrawableResource(R.drawable.bg_dialog);
        dialogListToppings.setCancelable(false);
        RecyclerView lvToppings = dialogListToppings.findViewById(R.id.rclToppings);
        TextView btnCancle = dialogListToppings.findViewById(R.id.btnCancel);
        TextView btnSave = dialogListToppings.findViewById(R.id.btnSave);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        lvToppings.setLayoutManager(layoutManager);
        adapterOptionTopping = new AdapterOptionTopping(context, menu.getRelishArrayList(), this, this);
        lvToppings.setAdapter(adapterOptionTopping);

        btnCancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogListToppings.dismiss();
            }
        });
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogListToppings.dismiss();
            }
        });
        dialogListToppings.show();
    }

    private void showListQuantities(final Menu food) {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_setquantity_new);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = 4 * (context.getResources().getDisplayMetrics().widthPixels / 5);
        dialog.getWindow().setAttributes(lp);
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_dialog);
        dialog.setCancelable(false);

        ListView lsvCook = (ListView) dialog.findViewById(R.id.lsvCookMethod);
        Integer[] numberlist = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        ArrayAdapter<Integer> adapter = new ArrayAdapter<Integer>(context,
                android.R.layout.simple_list_item_single_choice, numberlist);
        lsvCook.setAdapter(adapter);
        lsvCook.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                String a = 1 + position + "";
                food.setOrderNumber(Integer.parseInt(a));
                dialog.dismiss();
                notifyDataSetChanged();
            }
        });
        dialog.show();
    }

    public interface deleteItem {
        void deteteItem(Menu menu);
    }
}
