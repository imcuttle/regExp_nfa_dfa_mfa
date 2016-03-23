<%--
  Created by IntelliJ IDEA.
  User: Yc
  Date: 2016/3/21
  Time: 16:08
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Regular Expression Show</title>
    <style>
        body {
            width: 100%;
            height: 900px;
        }

        .container {

            text-align: left;
            margin-top: 20px;
        }

        .btn-show {
            padding: 5px 10px 5px;
        }

        circle {
            fill: none;
            stroke: red;
        }

        circle[name=start] {
            stroke: green;
        }

        circle[name=other] {
            stroke: blue;
        }

        svg {
            width: 33%;
        }

        line {
            stroke: rgb(99, 99, 99);
        }
    </style>
    <script src="//cdn.bootcss.com/jquery/3.0.0-beta1/jquery.min.js"></script>

</head>
<body>
<div class="container">
    <input id="input" type="text" placeholder="Input regular expression."/>
    <button class="btn-show">Show</button><h4>绿圈->开始状态；蓝圈->中间状态；双层红圈->终止状态；红蓝圈->既是开始亦是终止</h4>
    <div>
        <div>
            <svg id="nfa-show" viewBox="0 0 400 800">

            </svg>
            <svg id="dfa-show" viewBox="0 0 400 800">

            </svg>
            <svg id="mfa-show" viewBox="0 0 400 800">

            </svg>
        </div>
        <div>

        </div>
    </div>
</div>
</body>
<script>
    $('.btn-show').click(function () {
        var v = $('#input').val().trim();
        if(v=='') return;
        $.getJSON('get.do',{act:'inputRE',value:v},function(d){
            if(d.nfa)
                $('#nfa-show').empty().drawFA(d.nfa, 50, 100);
            if(d.dfa)
                $('#dfa-show').empty().drawFA(d.dfa, 50, 100);
            if(d.mfa)
                $('#mfa-show').empty().drawFA(d.mfa, 50, 100);
            if(d=='error')
                alert('parse error!');
        })
    })
    $(document).on('keypress',function(e){
        if(e.keyCode==13){
            $('.btn-show').click()
        }
    })
    var svgns = "http://www.w3.org/2000/svg";
    function createArrowLine(x1, y1, x2, y2, type, tag) {
        var lineArrow = '<svg name="lineArrow" tag=' + tag + ' name=' + type + '>' +
                '<defs>' +
                '<marker id="markerCircle" markerWidth="8" markerHeight="8" refx="5" refy="5">' +
                '<circle cx="5" cy="5" r="3" style="stroke: none; fill:#000000;"/>' +
                '</marker>' +
                '<marker id="arrow" markerUnits="strokeWidth" markerWidth="12" markerHeight="12" viewBox="0 0 12 12" refX="6" refY="6" orient="auto">' +
                '<path d="M2,2 L10,6 L2,10 L6,6 L2,2" style="fill: #000000;"/>' +
                '</marker>' +
                '<marker id="markerArrow" markerWidth="13" markerHeight="13" refx="2" refy="6" orient="auto">' +
                '<path d="M2,2 L2,11 L10,6 L2,2" style="fill: #000000;" />' +
                '</marker>' +
                '</defs>' +
                '<line x1="' + x1 + '" y1="' + y1 + '" x2="' + x2 + '" y2="' + y2 + '"  stroke="red" stroke-width="1" marker-start="url(#markerCircle)" marker-end="url(#markerArrow)"/>' +
                '</svg>'

        return lineArrow;
    }
    function createCurveArrowLine(x1, y1, x2, y2,name,tag, delta,offsetX,offsetY) {
        delta = delta==undefined?1:delta;
//        if(delta<0){var t1 = x1,t2 = y1;x1=x2;y1=y2;x2=t1;y2=t2;}
        var CurveArrowLine = '<svg name='+name+' tag='+tag+'>' +
                '<defs>' +
                '<marker id="markerCircle" markerWidth="8" markerHeight="8" refx="5" refy="5">' +
                '<circle cx="5" cy="5" r="3" style="stroke: none; fill:#000000;"/>' +
                '</marker>' +
                '<marker id="arrow" markerUnits="strokeWidth" markerWidth="12" markerHeight="12" viewBox="0 0 12 12" refX="6" refY="6" orient="auto">' +
                '<path d="M2,2 L10,6 L2,10 L6,6 L2,2" style="fill: #000000;"/>' +
                '</marker>' +
                '<marker id="markerArrow" markerWidth="13" markerHeight="13" refx="2" refy="6" orient="auto">' +
                '<path d="M2,2 L2,11 L10,6 L2,2" style="fill: #000000;" />' +
                '</marker>' +
                '</defs>' +
                '<path d="M' + [x1, y1].join(',') + ' C' + [x1 + delta*offsetX, y1 + delta*offsetY].join(' ') + ' ' + [x2 - delta*offsetX, y2 + delta*offsetY].join(' ') + ' ' + [x2, y2].join(' ') + '"' +
                'style="stroke: #6666ff; stroke-width: 1px; fill: none;' +
                'marker-start: url(#markerCircle);' +
                'marker-mid:url(#arrow);' +
                'marker-end: url(#markerArrow)" />' +
                '</svg>';
        return CurveArrowLine;
    }
    /*

     */
    function createText(x, y, text, size) {
        size = size == undefined ? 'small' : size;
        return '<svg><text style="stroke: black; font-size: ' + size + '" x="' + x + '" y="' + y + '" style="fill:red;">'
                + text + '</text><svg>';
    }
    function createCircle(x, y, r, name, tag) {
        var c = $(document.createElementNS(svgns, 'circle'));
        c.attr('cx', x).attr('cy', y).attr('r', r).attr('name', name).attr('tag', tag);
        return c;
    }
    function createEndCircle(x, y, r, name, tag) {
        var c1 = createCircle(x, y, r, name, tag);
        var c2 = createCircle(x, y, r + 5, name, tag);
        return [c1, c2];
    }
    function createLine(x1, y1, x2, y2, name, tag) {
        var l = $(document.createElementNS(svgns, 'line'));
        l.attr('x1', x1).attr('y1', y1)
                .attr('x2', x2).attr('y2', y2)
                .attr('name', name).attr('tag', tag);
        return l;
    }
    function createPolyline(points) {
        var pl = $(document.createElementNS(svgns, 'polyline'));
        var str = '';
        for (var i = 0; i < points.length; i++) {
            str += ' ' + points[i].join(',');
        }
        pl.attr('points', str);
    }
    function convertDegree(degree) {
        return degree / 180 * Math.PI;
    }
    $.fn.getCirclePos = function () {
        return [parseInt($(this).attr('cx')), parseInt($(this).attr('cy'))];
    };
    $.fn.getR = function () {
        return parseInt($(this).attr('r'));
    }
    $.fn.drawFA = function (data, x, y) {
        //$(this).empty();
        $(this).append(createText(30,30,data.title,'noraml'))
        data.title=$(this).attr('id');
        var vexs = data.vertexes;
        var topo = data.topolist;
        var width = $(this)[0].viewBox.baseVal.width;
        var c = 0;
        var initX=x,initY=y;
        var delaX = 70, delaY = 40, r = 15, ends = [];
        x = x - delaX;
        var low = x;
        y -= delaY;
        for (var i = 0; i < topo.length; i++) {
            var vex = vexs[topo[i]], shape;
            if(vex.data==null) continue;
            if(x>width-delaX-20){
                c++;
                delaX = -Math.abs(delaX);
                y = initY + c*2*Math.abs(delaY);
            }else if(x+delaX<=low+10&&i>0){
                c++;
                delaX = Math.abs(delaX);
                y = initY + (c*2)*Math.abs(delaY);
            }
            if (vex.status == 'start') {
                shape = createCircle(x += delaX, y += delaY, r, data.title +vex.data, vex.status);
                shape.css({'stroke': 'green', fill: '', 'stroke-width': '1.5'});
            } else if (vex.status == 'end') {
                shape = createEndCircle(x += delaX, y+=delaY, r,data.title + vex.data, vex.status)
                shape.forEach(function (e) {
                    e.css({'stroke': 'red', fill: '', 'stroke-width': '1.5'});
                });
            } else if(vex.status == 'other'){
                shape = createCircle(x += delaX, y += delaY, r, data.title +vex.data, vex.status);
                shape.css({'stroke': 'blue', fill: '', 'stroke-width': '1.5'});
            }else{
                shape = createEndCircle(x += delaX, y += delaY, r,data.title + vex.data, vex.status);
                shape[0].css({'stroke': 'blue', fill: '', 'stroke-width': '1.5'})
                shape[1].css({'stroke': 'red', fill: '', 'stroke-width': '1.5'})
            }
            var text = createText(x - 4, y + 4, vex.data);
            $(this).append(shape).append(text);
            delaY = -delaY;
        }
//        var _t = $(this)
//        if(ends.length!=0)
//            ends.forEach(function (end) {
//                delaY = -delaY;
//            })
        //drawLine
        for (var i = 0; i < vexs.length; i++) {
            var vex = vexs[i], from = $('[name=' +data.title + vex.data + ']');
            var edges = vex.edges;
            if(edges==null) continue;
            for (var j = 0; j < edges.length; j++) {
                var edge = edges[j];
                var char = String.fromCharCode(edge.weight);
                var to = $('[name=' +data.title +vexs[edge.vertex].data + ']');
                var fromPos = from.getCirclePos(), toPos = to.getCirclePos();
                var tag = data.title + from.attr('name') + '-' + to.attr('name');
                var fromR = from.getR(), toR = to.getR();
                var l = $('[tag=' + tag + ']').length;

                if (from.attr('name') == to.attr('name')) {
                    if(l==0)
                        l = 1;
                    else
                        l = l % 2 == 1 ? -l  : l;
                    var d1 = l * fromR, d2 = l * toR;

                    fromPos[0] += d1;
                    fromPos[1] += d1;
                    toPos[0] -= d2;
                    toPos[1] += d2;
                    $(this).append(createCurveArrowLine(fromPos[0], fromPos[1], toPos[0], toPos[1], 'line', tag, l, 30, 25));
                    $(this).append(createText((fromPos[0] + toPos[0]) / 2, (fromPos[1] + toPos[1]) / 2+25*l+3, char, 'smaller'));
                } else {
                    if(fromPos[0]<toPos[0])
                    {fromPos[0] += fromR;toPos[0]-= toR+4;}
                    else
                    {fromPos[0] -= fromR;toPos[0]+= toR+4;}
                    if(l!=0) {
                        l = l % 2 == 1 ? -l : l;
                        fromPos[1] -= (l *.5)*fromR;toPos[1]-= (l*.5)*toR;
                        $(this).append(createCurveArrowLine(fromPos[0], fromPos[1], toPos[0], toPos[1], 'line', tag,l,-10,-7))
                    }else{
                        $(this).append(createArrowLine(fromPos[0], fromPos[1], toPos[0], toPos[1], 'line', tag))
                    }
                    $(this).append(createText((fromPos[0] + toPos[0]) / 2, (fromPos[1] + toPos[1]) / 2, char, 'smaller'));
                }
            }
        }
    }
//    var data ={"type":"dinetwork","topolist":[0,1,2,4,3,5],"title":"MFA","vertexes":[{"data":"S","status":"start","edges":[{"vertex":2,"weight":98},{"vertex":1,"weight":97}]},{"data":"A","status":"other","edges":[{"vertex":3,"weight":98},{"vertex":1,"weight":97}]},{"data":"B","status":"end","edges":[{"vertex":4,"weight":98}]},{"data":"C","status":"end","edges":[{"vertex":5,"weight":98}]},{"data":"D","status":"end","edges":[{"vertex":4,"weight":98},{"vertex":3,"weight":97}]},{"data":"F","status":"other","edges":[{"vertex":3,"weight":97}]},{}]}
//    $('#nfa-show').drawFA(data,60,80);
</script>
</html>
