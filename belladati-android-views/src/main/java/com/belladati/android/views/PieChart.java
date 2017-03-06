package com.belladati.android.views;

import android.content.Context;
import android.graphics.Color;
import android.text.SpannableString;
import android.util.AttributeSet;

import com.belladati.httpclientandroidlib.client.utils.URIBuilder;
import com.belladati.sdk.BellaDatiService;
import com.belladati.sdk.impl.BellaDatiServiceWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;

import java.util.ArrayList;
import java.util.List;

import static com.github.mikephil.charting.utils.ColorTemplate.rgb;

/**
 * Created by KarimT on 30.11.2016.
 */
public class PieChart extends com.github.mikephil.charting.charts.PieChart {


    public void setCenterText(String text) {
        this.text = text;
    }

    private String text;

    public void setLegendColour(int legendColour) {
        this.legendColour = legendColour;
    }

    private int legendColour;

    public void setIdChart(String idChart) {
        this.idChart = idChart;
    }

    private String idChart;

    public void setValFromRepToCenter(boolean valFromRepToCenter) {
        this.valFromRepToCenter = valFromRepToCenter;
    }

    private boolean valFromRepToCenter;

    public void setHideValues(boolean hideValues) {
        this.hideValues = hideValues;
    }

    private boolean hideValues;

    public void setHideLegend(boolean hideLegend) {
        this.hideLegend = hideLegend;
    }

    private boolean hideLegend;


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

    public PieChart(Context context) {
        super(context);
    }

    public PieChart(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PieChart(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void createPieChart() throws Exception {
        createPieChart(null);
    }

    public void createPieChart(String additionalUriParam) throws Exception {
        if (service == null) {
            throw new Exception("Service must be set up");
        }

        if (idChart == null) {
            throw new Exception("Detected no chart id");
        }
        URIBuilder builder;
        if (additionalUriParam != null) {

            builder = new URIBuilder("api/reports/views/" + idChart + "/chart" + additionalUriParam);
        } else {
            builder = new URIBuilder("api/reports/views/" + idChart + "/chart");
        }

        if (filterNode != null) {
            ObjectNode drilldownNode = new ObjectMapper().createObjectNode();
            drilldownNode.put("drilldown", filterNode);

            builder.addParameter("filter", drilldownNode.toString());
        }

        JsonNode jn = wrapper.loadJson(builder.toString());

        JsonNode content = jn.findPath("content");
        JsonNode elements = content.findPath("elements").get(0).findPath("values");
        JsonNode colours = content.findPath("elements").get(0).findPath("colours");
        List<String> labels = new ArrayList<>();
        String valueForCenter = "";

        List<Entry> entries = new ArrayList<Entry>();
        for (int i = 0; i < elements.size(); i++) {

            entries.add(new Entry(elements.get(i).findPath("value").floatValue(), i));
            valueForCenter = elements.get(i).findPath("value").asText();
        }

        for (int k = 0; k < elements.size(); k++) {
            labels.add(elements.get(k).findPath("label").asText());
        }
        String[] labelasArray = new String[labels.size()];
        labelasArray = labels.toArray(labelasArray);
        PieDataSet pieDataSet = new PieDataSet(entries, " ");
        pieDataSet.setColors(getColors(colours));
        if (hideValues)
            pieDataSet.setDrawValues(false);

        PieData pieData = new PieData(labelasArray, pieDataSet);


        if (valFromRepToCenter)
            setCenterText(generateCenterSpannableText(valueForCenter));

        if (text != null)
            setCenterText(generateCenterSpannableText(text));

        if (hideValues)
            setDrawSliceText(false);

        setDescription("");
        setData(pieData);
        invalidate();
        if (!hideLegend) {
            Legend legend = getLegend();
            if (legendColour == 0)
                legendColour = Color.WHITE;
            legend.setTextColor(legendColour);
            legend.setWordWrapEnabled(true);
        }

    }

    private int[] getColors(JsonNode jnElements) {

        int stacksize = jnElements.size();
        // have as many colors as stack-values per entry
        int[] colors = new int[stacksize];

        ArrayList<Integer> colorList = new ArrayList<>();
        for (int i = 0; i < stacksize; i++) {
            colorList.add(rgb(jnElements.get(i).asText()));
        }
        Integer[] MATERIAL_COLORS = colorList.toArray(new Integer[colorList.size()]);
        for (int i = 0; i < colors.length; i++) {
            colors[i] = MATERIAL_COLORS[i];
        }

        return colors;
    }

    private SpannableString generateCenterSpannableText(String text) {

        SpannableString s = new SpannableString(text);

        return s;
    }


}