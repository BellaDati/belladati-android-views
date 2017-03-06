package com.belladati.android.views;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.belladati.httpclientandroidlib.client.utils.URIBuilder;
import com.belladati.sdk.BellaDatiService;
import com.belladati.sdk.impl.BellaDatiServiceWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;


/**
 * Created by KarimT on 28.11.2016.
 */
public class Table extends TableLayout {


    public String getTableName() {
        return tableName;
    }

    private String tableName;

    public void setHeadColumnStyle(int headColumnStyle) {
        this.headColumnStyle = headColumnStyle;
    }

    private int headColumnStyle;
    private int columnsBody, columnsHeader;

    public void setFilterNode(ObjectNode filterNode) {
        this.filterNode = filterNode;
    }

    private ObjectNode filterNode;

    public void setService(BellaDatiService service) {
        this.service = service;
        this.wrapper = new BellaDatiServiceWrapper(this.service);
    }

    private BellaDatiService service;
    private BellaDatiServiceWrapper wrapper;

    public void setIdTable(String idTable) {
        this.idTable = idTable;
    }

    private String idTable;

    public Table(Context context) {
        super(context);
    }

    public Table(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    public void createTable() throws Exception {
        createTable(null);

    }

    public void createTable(String additionalUriParam) throws Exception {

        if (service == null) {
            throw new Exception("Service must be set up");
        }

        if (idTable == null) {
            throw new Exception("Detected no table id");
        }

        URIBuilder builder;
        if (additionalUriParam != null) {
            builder = new URIBuilder("api/reports/views/" + idTable + "/table/json" + additionalUriParam);
        } else {
            builder = new URIBuilder("api/reports/views/" + idTable + "/table/json");
        }


        if (filterNode != null) {
            ObjectNode drilldownNode = new ObjectMapper().createObjectNode();
            drilldownNode.put("drilldown", filterNode);
            builder.addParameter("filter", drilldownNode.toString());
        }


        JsonNode jn = wrapper.loadJson(builder.toString());

        tableName = jn.findPath("name").asText();
        JsonNode jnHeader = jn.findPath("header");
        addView(getHeaderForTable(jnHeader), new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));

        JsonNode jnBody = jn.findPath("body");
        columnsBody = jnBody.size();
        for (int i = 0; i < columnsBody; i++) {

            /**Add row to TableLayout.**/
            addView(getBodyForTable(jnBody, i), new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
        }
    }

    private TableRow getBodyForTable(JsonNode jnBody, int i) {
        TableRow tr = new TableRow(getContext());
        tr.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        tr.setOrientation(LinearLayout.HORIZONTAL);

        for (int column = 0; column < jnBody.get(0).size(); column++) {

            TextView b = new TextView(getContext());
            b.setGravity(Gravity.CENTER);
            b.setText(jnBody.get(i).get(column).findPath("value").asText());
            b.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, 60));
            String style = jnBody.get(i).get(column).findPath("style").asText();

            if (style.equals("")) {
                b.setBackgroundResource(headColumnStyle);
            } else {
                String[] rgbBackground = style.split(";");
                rgbBackground[0] = rgbBackground[0].substring(rgbBackground[0].indexOf("(") + 1);
                rgbBackground[0] = rgbBackground[0].substring(0, rgbBackground[0].indexOf(")"));
                String[] rgbForeground = rgbBackground[0].replace(" ", "").split(",");
                b.setTextColor(Color.rgb(Integer.parseInt(rgbForeground[0]), Integer.parseInt(rgbForeground[1]), Integer.parseInt(rgbForeground[2])));
                rgbBackground[1] = rgbBackground[1].substring(rgbBackground[1].indexOf("(") + 1);
                rgbBackground[1] = rgbBackground[1].substring(0, rgbBackground[1].indexOf(")"));
                String[] rgb = rgbBackground[1].replace(" ", "").split(",");
                b.setBackgroundColor(Color.rgb(Integer.parseInt(rgb[0]), Integer.parseInt(rgb[1]), Integer.parseInt(rgb[2])));
            }
            tr.addView(b);
        }
        return tr;
    }

    private TableRow getHeaderForTable(JsonNode jnHeader) {
        columnsHeader = jnHeader.get(0).size();
        TableRow trHeader = new TableRow(getContext());

        for (int columnHeader = 0; columnHeader < columnsHeader; columnHeader++) {
            TextView cell = new TextView(getContext());
            cell.setText(jnHeader.get(0).get(columnHeader).findPath("value").asText());
            cell.setLayoutParams(new TableRow.LayoutParams(150, TableRow.LayoutParams.MATCH_PARENT));
            cell.setGravity(Gravity.CENTER);
            cell.setTextColor(Color.BLACK);

            cell.setBackgroundResource(headColumnStyle);
            trHeader.addView(cell);
        }
        return trHeader;
    }
}
