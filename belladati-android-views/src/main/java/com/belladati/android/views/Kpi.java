package com.belladati.android.views;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import com.belladati.httpclientandroidlib.client.utils.URIBuilder;
import com.belladati.sdk.BellaDatiService;
import com.belladati.sdk.impl.BellaDatiServiceWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Created by KarimT on 29.11.2016.
 */
public class Kpi extends LinearLayout {

    public Kpi(Context context) {
        super(context);
    }

    public Kpi(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public Kpi(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public Kpi(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setFilterNode(ObjectNode filterNode) {
        this.filterNode = filterNode;
    }

    private ObjectNode filterNode;
    public void setService(BellaDatiService service) {
        this.service = service;
        this.wrapper=new BellaDatiServiceWrapper(this.service);
    }

    private BellaDatiService service;
    private BellaDatiServiceWrapper wrapper;

    @Override
    public void setBackground(Drawable background) {
        this.background = background;
    }

    private Drawable background;

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    private int textColor;

    public void setIdKpi(String idKpi) {
        this.idKpi = idKpi;
    }

    private String idKpi;

    public void fillKpi() throws Exception {
        if(service==null)
        {
            throw new Exception("Service must be set up");
        }

        if(idKpi==null)
        {
            throw new Exception("Detected no chart id");
        }
        URIBuilder  builder = new URIBuilder("api/reports/views/"+idKpi+"/kpi");

        if(filterNode!=null)
        {
            ObjectNode drilldownNode = new ObjectMapper().createObjectNode();
            drilldownNode.put("drilldown", filterNode);
            builder.addParameter("filter", drilldownNode.toString());
        }

        JsonNode jsonNode = wrapper.loadJson(builder.toString()).findPath("values");
        for(int i=0;i<jsonNode.size();i++)
        {
            TextView tvItem = new TextView(getContext());
            tvItem.setText(jsonNode.get(i).findPath("caption").asText());
            tvItem.setTextSize(25);
            tvItem.setPadding(20, 20, 20, 20);
            tvItem.setLayoutParams(new TableLayout.LayoutParams(0, TableLayout.LayoutParams.WRAP_CONTENT, 1f));
            tvItem.setTextColor(Color.BLACK);
            LinearLayout linearItemsRow = new LinearLayout(getContext());
            linearItemsRow.setOrientation(LinearLayout.HORIZONTAL);
            linearItemsRow.setPadding(20,20,20,20);
            linearItemsRow.setBackground(background);
            LinearLayout.LayoutParams LLParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            linearItemsRow.setLayoutParams(LLParams);
            linearItemsRow.addView(tvItem);
            TextView tvPrize = new TextView(getContext());
            tvPrize.setText(jsonNode.get(i).findPath("numberValue").asText());
            tvPrize.setTextSize(25);
            tvPrize.setPadding(20, 20, 20, 20);
            tvPrize.setGravity(Gravity.RIGHT);
            tvPrize.setTextColor(textColor);
            linearItemsRow.addView(tvPrize);
            addView(linearItemsRow);
        }
    }
}
