/***
 * RedXPlay main javascript.
 */
$(function () {
    var chart;
    var chartMem;

    $(document).ready(function () {

        // define the options for CPU
        var options = {
            chart:{
                renderTo:'graphred'
            },
            title:{
                text:'Redis CPU'
            },
            subtitle:{
                text:'Live streamed as Server sent event'
            },
            xAxis:{
                type:'time',
                min:0,
                max:60
            },
            yAxis:{
                title:{
                    text:'CPU usage'
                },
                min:0,
                max:500
            },
            legend:{
                borderWidth:0
            },
            tooltip:{
                shared:true,
                crosshairs:true
            },
            plotOptions:{
                series:{
                    cursor:'pointer',
                    point:{
                        events:{
                            click:function () {
                                hs.htmlExpand(null, {
                                    pageOrigin:{
                                        x:this.pageX,
                                        y:this.pageY
                                    },
                                    headingText:this.series.name,
                                    maincontentText:Highcharts.dateFormat('%A, %b %e, %Y', this.x) + ':<br/> ' +
                                        this.y + ' visits',
                                    width:200
                                });
                            }
                        }
                    },
                    marker:{
                        lineWidth:1
                    }
                }
            },
            series:[
                {
                    name:'CPU Sys',
                    data:[]
                },
                {
                    name:'CPU User',
                    data:[]
                }
            ]
        };

// define the options
        var optionsMem = {
            chart:{
                renderTo:'graphmem'
            },
            title:{
                text:'Redis Memory'
            },
            subtitle:{
                text:'Live streamed as Server sent event'
            },
            xAxis:{
                type:'time',
                min:0
            },
            yAxis:{
                title:{
                    text:'Memory size'
                },
                min:0
            },
            legend:{
                borderWidth:0
            },
            tooltip:{
                shared:true,
                crosshairs:true
            },
            plotOptions:{
                series:{
                    cursor:'pointer',
                    point:{
                        events:{
                            click:function () {
                                hs.htmlExpand(null, {
                                    pageOrigin:{
                                        x:this.pageX,
                                        y:this.pageY
                                    },
                                    headingText:this.series.name,
                                    maincontentText:Highcharts.dateFormat('%A, %b %e, %Y', this.x) + ':<br/> ' +
                                        this.y + ' visits',
                                    width:200
                                });
                            }
                        }
                    },
                    marker:{
                        lineWidth:1
                    }
                }
            },
            series:[
                {
                    name:'Mem Sys',
                    data:[]
                },
                {
                    name:'Mem User',
                    data:[]
                }
            ]
        };

        chart = new Highcharts.Chart(options);
        chartMem = new Highcharts.Chart(optionsMem);

        // Server sent event
        var feed;

        if (!!window.EventSource) {
            feed = new EventSource('/stream');
            $("#status").html("Connected");
            $("#disconnect").removeAttr("disabled");

            feed.addEventListener('generalInfo', function (e) {
                $("#status").html("Received general info data");
                var dataParsed = JSON.parse(e.data);

                $("#redis_version").html(dataParsed.redis_version)
                $("#uptime_in_seconds").html(dataParsed.uptime_in_seconds)
                $("#uptime_in_days").html(dataParsed.uptime_in_days)
            }, false);

            feed.addEventListener('memoryAndCpu', function (e) {
                $("#status").html("Received cpu and memory data");
                var dataParsed = JSON.parse(e.data);
                $("#used_cpu_sys").html(dataParsed.used_cpu_sys);
                $("#used_cpu_user").html(dataParsed.used_cpu_user);
                $("#used_cpu_sys_children").html(dataParsed.used_cpu_sys_children);
                $("#used_cpu_user_children").html(dataParsed.used_cpu_user_children);
                $("#used_memory_human").html(dataParsed.used_memory_human);
                $("#used_memory_peak_human").html(dataParsed.used_memory_peak_human);

                var point = dataParsed.used_cpu_sys;
                var point2 = dataParsed.used_cpu_user;
                var series = chart.series[0];
                var series2 = chart.series[1];
                var shift = series.data.length > 60; // shift if the series is longer than 60
                var shift2 = series2.data.length > 60; // shift if the series is longer than 60
                // add the point
                chart.series[0].addPoint(eval(point), true, shift);
                chart.series[1].addPoint(eval(point2), true, shift2);

                var pointM = dataParsed.used_memory;
                var pointM2 = dataParsed.used_memory_peak;
                var seriesM = chartMem.series[0];
                var seriesM2 = chartMem.series[1];
                var shiftM = seriesM.data.length > 60; // shift if the series is longer than 60
                var shiftM2 = seriesM2.data.length > 60; // shift if the series is longer than 60
                // add the point
                chartMem.series[0].addPoint(eval(pointM), true, shiftM);
                chartMem.series[1].addPoint(eval(pointM2), true, shiftM2);

                $("#mem_fragmentation_ratio").html(dataParsed.mem_fragmentation_ratio);
            }, false);


            feed.addEventListener('error', function (e) {
                if (e.readyState == EventSource.CLOSED) {
                    // Connection was closed.
                    $("#status").html("Error")
                }
            }, false);

        }
        else {
            $("#status").html("Server side event not supported by this browser");
        }
        $(document).ready(function () {
            $("#disconnect").click(function (e) {
                e.preventDefault();
                feed.close();
                $("#status").html("Disconnected");
                $("#disconnect").attr("disabled", "disabled");
            });
        });


    });

})



