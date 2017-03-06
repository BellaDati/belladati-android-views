package com.belladati.android.views;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;

import com.belladati.httpclientandroidlib.client.utils.URIBuilder;
import com.belladati.sdk.BellaDatiService;
import com.belladati.sdk.impl.BellaDatiServiceWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.XAxis.XAxisPosition;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;

import java.util.ArrayList;
import java.util.List;

import static com.github.mikephil.charting.utils.ColorTemplate.rgb;

/**
 * Created by KarimT on 28.11.2016.
 */
public class StackedBarChart extends com.github.mikephil.charting.charts.BarChart {


    public void setAxesTextColour(int axesTextColour) {
        this.axesTextColour = axesTextColour;
    }

    private int axesTextColour;

    public void setIdChart(String idChart) {
        this.idChart = idChart;
    }

    private String idChart;

    public void setLegendColour(int legendColour) {
        this.legendColour = legendColour;
    }

    private int legendColour;

    public void setValueTextColor(int valueTextColor) {
        this.valueTextColor = valueTextColor;
    }

    private int valueTextColor;

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

    public StackedBarChart(Context context) {
        super(context);
    }

    public StackedBarChart(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public StackedBarChart(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void createStackedBarChart() throws Exception {
        createStackedBarChart(null);
    }

    public void createStackedBarChart(String additionalUriParam) throws Exception {
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
        JsonNode x_axis = content.findPath("x_axis").findPath("labels").findPath("labels");

        setupChart();
        JsonNode elements = content.findPath("elements").get(0).findPath("keys");
        JsonNode elementsVal = content.findPath("elements").get(0).findPath("values");
        List<String> labels = new ArrayList<>();
        ArrayList<BarEntry> lists = new ArrayList<BarEntry>();

        for (int i = 0; i < elementsVal.size(); i++) {
            List<Float> list = new ArrayList<>();
            for (int j = 0; j < elements.size(); j++) {
                list.add(elementsVal.get(i).get(j).findPath("val").floatValue());
            }
            float[] valueF = new float[list.size()];
            int x = 0;
            for (Float f : list) {
                valueF[x++] = (f != null ? f : Float.NaN); // Or whatever default you want.
            }
            lists.add(new BarEntry(valueF, i));
        }


        for (int k = 0; k < elements.size(); k++) {
            labels.add(elements.get(k).findPath("text").asText());
        }
        String[] labelasArray = new String[labels.size()];
        labelasArray = labels.toArray(labelasArray);

        BarDataSet set1 = new BarDataSet(lists, " ");
        set1.setColors(getColors(elements));
        set1.setStackLabels(labelasArray);

        ArrayList<IBarDataSet> dataSets = new ArrayList<IBarDataSet>();
        dataSets.add(set1);
        List<String> h_lables = new ArrayList<>();

        for (int l = 0; l < x_axis.size(); l++) {
            h_lables.add(x_axis.get(l).asText());
        }
        String[] xAxis = new String[h_lables.size()];
        xAxis = h_lables.toArray(xAxis);

        BarData data = new BarData(xAxis, dataSets);
        if (valueTextColor == 0)
            valueTextColor = Color.WHITE;
        data.setValueTextColor(valueTextColor);
        data.setValueFormatter(new MyValueFormatter());
        setDescription("");
        setData(data);

        Legend legend = getLegend();
        if (legendColour == 0)
            legendColour = Color.WHITE;
        legend.setTextColor(legendColour);
        legend.setWordWrapEnabled(true);
    }

    private int[] getColors(JsonNode jnElements) {

        int stacksize = jnElements.size();
        // have as many colors as stack-values per entry
        int[] colors = new int[stacksize];

        ArrayList<Integer> colorList = new ArrayList<>();
        for (int i = 0; i < stacksize; i++) {
            colorList.add(rgb(jnElements.get(i).findPath("colour").asText()));
        }
        Integer[] MATERIAL_COLORS = colorList.toArray(new Integer[colorList.size()]);
        for (int i = 0; i < colors.length; i++) {
            colors[i] = MATERIAL_COLORS[i];
        }

        return colors;
    }

    private void setupChart() {
        if (axesTextColour == 0)
            axesTextColour = Color.WHITE;
        setMaxVisibleValueCount(40);
        // scaling can now only be done on x- and y-axis separately
        setPinchZoom(false);

        setDrawGridBackground(false);
        setDrawBarShadow(false);

        setDrawValueAboveBar(false);
        setHighlightFullBarEnabled(false);

        YAxis leftAxis = getAxisLeft();
        leftAxis.setStartAtZero(true); // this replaces setStartAtZero(true)
        leftAxis.setTextColor(axesTextColour);
        getAxisRight().setEnabled(false);

        XAxis xLabels = getXAxis();
        xLabels.setTextColor(axesTextColour);
        xLabels.setPosition(XAxisPosition.TOP);
    }
}
