/*
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
$(document).ready(function() {

    $(".click-title").mouseenter( function(    e){
        e.preventDefault();
        this.style.cursor="pointer";
    });
    $(".click-title").mousedown( function(event){
        event.preventDefault();
    });

    // Ugly code while this script is shared among several pages
    try{
        refreshHitsPerSecond(true);
    } catch(e){}
    try{
        refreshResponseTimeOverTime(true);
    } catch(e){}
    try{
        refreshResponseTimePercentiles();
    } catch(e){}
});


var responseTimePercentilesInfos = {
        data: {"result": {"minY": 122.0, "minX": 0.0, "maxY": 3996.0, "series": [{"data": [[0.0, 122.0], [0.1, 131.0], [0.2, 165.0], [0.3, 165.0], [0.4, 166.0], [0.5, 167.0], [0.6, 175.0], [0.7, 176.0], [0.8, 182.0], [0.9, 188.0], [1.0, 202.0], [1.1, 209.0], [1.2, 210.0], [1.3, 212.0], [1.4, 212.0], [1.5, 213.0], [1.6, 217.0], [1.7, 218.0], [1.8, 218.0], [1.9, 221.0], [2.0, 221.0], [2.1, 224.0], [2.2, 228.0], [2.3, 232.0], [2.4, 235.0], [2.5, 247.0], [2.6, 247.0], [2.7, 248.0], [2.8, 251.0], [2.9, 255.0], [3.0, 256.0], [3.1, 257.0], [3.2, 258.0], [3.3, 259.0], [3.4, 260.0], [3.5, 262.0], [3.6, 262.0], [3.7, 263.0], [3.8, 265.0], [3.9, 265.0], [4.0, 274.0], [4.1, 288.0], [4.2, 291.0], [4.3, 297.0], [4.4, 300.0], [4.5, 303.0], [4.6, 303.0], [4.7, 303.0], [4.8, 307.0], [4.9, 308.0], [5.0, 309.0], [5.1, 311.0], [5.2, 314.0], [5.3, 316.0], [5.4, 317.0], [5.5, 324.0], [5.6, 325.0], [5.7, 325.0], [5.8, 325.0], [5.9, 326.0], [6.0, 343.0], [6.1, 345.0], [6.2, 345.0], [6.3, 346.0], [6.4, 347.0], [6.5, 348.0], [6.6, 356.0], [6.7, 358.0], [6.8, 359.0], [6.9, 361.0], [7.0, 362.0], [7.1, 363.0], [7.2, 364.0], [7.3, 364.0], [7.4, 365.0], [7.5, 367.0], [7.6, 380.0], [7.7, 382.0], [7.8, 383.0], [7.9, 383.0], [8.0, 387.0], [8.1, 393.0], [8.2, 396.0], [8.3, 398.0], [8.4, 398.0], [8.5, 402.0], [8.6, 402.0], [8.7, 404.0], [8.8, 405.0], [8.9, 409.0], [9.0, 411.0], [9.1, 411.0], [9.2, 412.0], [9.3, 414.0], [9.4, 414.0], [9.5, 420.0], [9.6, 422.0], [9.7, 425.0], [9.8, 433.0], [9.9, 434.0], [10.0, 437.0], [10.1, 440.0], [10.2, 442.0], [10.3, 452.0], [10.4, 455.0], [10.5, 455.0], [10.6, 455.0], [10.7, 458.0], [10.8, 460.0], [10.9, 461.0], [11.0, 469.0], [11.1, 470.0], [11.2, 474.0], [11.3, 475.0], [11.4, 482.0], [11.5, 483.0], [11.6, 485.0], [11.7, 487.0], [11.8, 499.0], [11.9, 500.0], [12.0, 505.0], [12.1, 506.0], [12.2, 507.0], [12.3, 507.0], [12.4, 510.0], [12.5, 511.0], [12.6, 512.0], [12.7, 518.0], [12.8, 525.0], [12.9, 529.0], [13.0, 530.0], [13.1, 532.0], [13.2, 538.0], [13.3, 540.0], [13.4, 545.0], [13.5, 549.0], [13.6, 551.0], [13.7, 552.0], [13.8, 553.0], [13.9, 555.0], [14.0, 557.0], [14.1, 557.0], [14.2, 557.0], [14.3, 559.0], [14.4, 561.0], [14.5, 561.0], [14.6, 565.0], [14.7, 567.0], [14.8, 567.0], [14.9, 568.0], [15.0, 571.0], [15.1, 571.0], [15.2, 572.0], [15.3, 573.0], [15.4, 576.0], [15.5, 577.0], [15.6, 579.0], [15.7, 581.0], [15.8, 584.0], [15.9, 596.0], [16.0, 597.0], [16.1, 600.0], [16.2, 606.0], [16.3, 615.0], [16.4, 615.0], [16.5, 616.0], [16.6, 621.0], [16.7, 623.0], [16.8, 628.0], [16.9, 629.0], [17.0, 633.0], [17.1, 633.0], [17.2, 635.0], [17.3, 635.0], [17.4, 638.0], [17.5, 639.0], [17.6, 640.0], [17.7, 647.0], [17.8, 648.0], [17.9, 658.0], [18.0, 668.0], [18.1, 669.0], [18.2, 670.0], [18.3, 672.0], [18.4, 673.0], [18.5, 675.0], [18.6, 679.0], [18.7, 683.0], [18.8, 689.0], [18.9, 690.0], [19.0, 692.0], [19.1, 693.0], [19.2, 693.0], [19.3, 696.0], [19.4, 697.0], [19.5, 699.0], [19.6, 699.0], [19.7, 700.0], [19.8, 708.0], [19.9, 714.0], [20.0, 722.0], [20.1, 724.0], [20.2, 724.0], [20.3, 725.0], [20.4, 725.0], [20.5, 735.0], [20.6, 735.0], [20.7, 736.0], [20.8, 738.0], [20.9, 741.0], [21.0, 744.0], [21.1, 746.0], [21.2, 748.0], [21.3, 760.0], [21.4, 761.0], [21.5, 765.0], [21.6, 768.0], [21.7, 768.0], [21.8, 770.0], [21.9, 771.0], [22.0, 774.0], [22.1, 782.0], [22.2, 782.0], [22.3, 783.0], [22.4, 786.0], [22.5, 788.0], [22.6, 790.0], [22.7, 794.0], [22.8, 794.0], [22.9, 808.0], [23.0, 810.0], [23.1, 817.0], [23.2, 825.0], [23.3, 827.0], [23.4, 827.0], [23.5, 832.0], [23.6, 835.0], [23.7, 836.0], [23.8, 837.0], [23.9, 838.0], [24.0, 839.0], [24.1, 840.0], [24.2, 843.0], [24.3, 846.0], [24.4, 848.0], [24.5, 852.0], [24.6, 860.0], [24.7, 860.0], [24.8, 863.0], [24.9, 867.0], [25.0, 871.0], [25.1, 881.0], [25.2, 884.0], [25.3, 894.0], [25.4, 898.0], [25.5, 900.0], [25.6, 901.0], [25.7, 902.0], [25.8, 904.0], [25.9, 908.0], [26.0, 912.0], [26.1, 913.0], [26.2, 916.0], [26.3, 916.0], [26.4, 916.0], [26.5, 918.0], [26.6, 920.0], [26.7, 923.0], [26.8, 926.0], [26.9, 926.0], [27.0, 927.0], [27.1, 927.0], [27.2, 928.0], [27.3, 930.0], [27.4, 932.0], [27.5, 937.0], [27.6, 938.0], [27.7, 952.0], [27.8, 953.0], [27.9, 957.0], [28.0, 961.0], [28.1, 965.0], [28.2, 971.0], [28.3, 972.0], [28.4, 973.0], [28.5, 979.0], [28.6, 982.0], [28.7, 991.0], [28.8, 992.0], [28.9, 994.0], [29.0, 997.0], [29.1, 1004.0], [29.2, 1005.0], [29.3, 1010.0], [29.4, 1011.0], [29.5, 1013.0], [29.6, 1014.0], [29.7, 1015.0], [29.8, 1021.0], [29.9, 1029.0], [30.0, 1030.0], [30.1, 1038.0], [30.2, 1038.0], [30.3, 1044.0], [30.4, 1044.0], [30.5, 1045.0], [30.6, 1045.0], [30.7, 1045.0], [30.8, 1047.0], [30.9, 1049.0], [31.0, 1051.0], [31.1, 1056.0], [31.2, 1060.0], [31.3, 1060.0], [31.4, 1061.0], [31.5, 1061.0], [31.6, 1064.0], [31.7, 1064.0], [31.8, 1068.0], [31.9, 1070.0], [32.0, 1071.0], [32.1, 1072.0], [32.2, 1073.0], [32.3, 1074.0], [32.4, 1074.0], [32.5, 1074.0], [32.6, 1076.0], [32.7, 1084.0], [32.8, 1086.0], [32.9, 1087.0], [33.0, 1088.0], [33.1, 1091.0], [33.2, 1092.0], [33.3, 1097.0], [33.4, 1101.0], [33.5, 1102.0], [33.6, 1103.0], [33.7, 1104.0], [33.8, 1105.0], [33.9, 1109.0], [34.0, 1112.0], [34.1, 1116.0], [34.2, 1118.0], [34.3, 1121.0], [34.4, 1126.0], [34.5, 1126.0], [34.6, 1130.0], [34.7, 1131.0], [34.8, 1133.0], [34.9, 1135.0], [35.0, 1140.0], [35.1, 1146.0], [35.2, 1147.0], [35.3, 1150.0], [35.4, 1150.0], [35.5, 1152.0], [35.6, 1152.0], [35.7, 1167.0], [35.8, 1168.0], [35.9, 1168.0], [36.0, 1171.0], [36.1, 1173.0], [36.2, 1173.0], [36.3, 1174.0], [36.4, 1187.0], [36.5, 1190.0], [36.6, 1193.0], [36.7, 1195.0], [36.8, 1197.0], [36.9, 1201.0], [37.0, 1202.0], [37.1, 1203.0], [37.2, 1205.0], [37.3, 1205.0], [37.4, 1209.0], [37.5, 1210.0], [37.6, 1211.0], [37.7, 1213.0], [37.8, 1214.0], [37.9, 1221.0], [38.0, 1224.0], [38.1, 1225.0], [38.2, 1228.0], [38.3, 1232.0], [38.4, 1235.0], [38.5, 1239.0], [38.6, 1240.0], [38.7, 1240.0], [38.8, 1245.0], [38.9, 1248.0], [39.0, 1248.0], [39.1, 1251.0], [39.2, 1255.0], [39.3, 1261.0], [39.4, 1263.0], [39.5, 1265.0], [39.6, 1266.0], [39.7, 1267.0], [39.8, 1271.0], [39.9, 1275.0], [40.0, 1278.0], [40.1, 1278.0], [40.2, 1282.0], [40.3, 1283.0], [40.4, 1290.0], [40.5, 1298.0], [40.6, 1309.0], [40.7, 1310.0], [40.8, 1314.0], [40.9, 1320.0], [41.0, 1322.0], [41.1, 1324.0], [41.2, 1336.0], [41.3, 1339.0], [41.4, 1342.0], [41.5, 1345.0], [41.6, 1346.0], [41.7, 1347.0], [41.8, 1358.0], [41.9, 1363.0], [42.0, 1365.0], [42.1, 1372.0], [42.2, 1380.0], [42.3, 1380.0], [42.4, 1380.0], [42.5, 1382.0], [42.6, 1384.0], [42.7, 1384.0], [42.8, 1384.0], [42.9, 1389.0], [43.0, 1393.0], [43.1, 1394.0], [43.2, 1395.0], [43.3, 1396.0], [43.4, 1396.0], [43.5, 1405.0], [43.6, 1408.0], [43.7, 1413.0], [43.8, 1414.0], [43.9, 1424.0], [44.0, 1427.0], [44.1, 1427.0], [44.2, 1431.0], [44.3, 1433.0], [44.4, 1438.0], [44.5, 1438.0], [44.6, 1446.0], [44.7, 1449.0], [44.8, 1451.0], [44.9, 1454.0], [45.0, 1456.0], [45.1, 1458.0], [45.2, 1458.0], [45.3, 1459.0], [45.4, 1463.0], [45.5, 1463.0], [45.6, 1464.0], [45.7, 1465.0], [45.8, 1468.0], [45.9, 1468.0], [46.0, 1473.0], [46.1, 1477.0], [46.2, 1478.0], [46.3, 1479.0], [46.4, 1479.0], [46.5, 1482.0], [46.6, 1482.0], [46.7, 1483.0], [46.8, 1483.0], [46.9, 1487.0], [47.0, 1488.0], [47.1, 1490.0], [47.2, 1496.0], [47.3, 1497.0], [47.4, 1497.0], [47.5, 1500.0], [47.6, 1500.0], [47.7, 1503.0], [47.8, 1504.0], [47.9, 1506.0], [48.0, 1511.0], [48.1, 1511.0], [48.2, 1512.0], [48.3, 1515.0], [48.4, 1516.0], [48.5, 1521.0], [48.6, 1522.0], [48.7, 1523.0], [48.8, 1524.0], [48.9, 1528.0], [49.0, 1532.0], [49.1, 1534.0], [49.2, 1536.0], [49.3, 1541.0], [49.4, 1542.0], [49.5, 1543.0], [49.6, 1545.0], [49.7, 1548.0], [49.8, 1548.0], [49.9, 1552.0], [50.0, 1554.0], [50.1, 1554.0], [50.2, 1556.0], [50.3, 1558.0], [50.4, 1558.0], [50.5, 1561.0], [50.6, 1562.0], [50.7, 1566.0], [50.8, 1566.0], [50.9, 1567.0], [51.0, 1571.0], [51.1, 1576.0], [51.2, 1577.0], [51.3, 1578.0], [51.4, 1578.0], [51.5, 1581.0], [51.6, 1586.0], [51.7, 1587.0], [51.8, 1588.0], [51.9, 1589.0], [52.0, 1590.0], [52.1, 1592.0], [52.2, 1594.0], [52.3, 1599.0], [52.4, 1603.0], [52.5, 1604.0], [52.6, 1609.0], [52.7, 1613.0], [52.8, 1613.0], [52.9, 1614.0], [53.0, 1615.0], [53.1, 1620.0], [53.2, 1620.0], [53.3, 1629.0], [53.4, 1631.0], [53.5, 1631.0], [53.6, 1639.0], [53.7, 1643.0], [53.8, 1649.0], [53.9, 1649.0], [54.0, 1650.0], [54.1, 1654.0], [54.2, 1654.0], [54.3, 1655.0], [54.4, 1660.0], [54.5, 1661.0], [54.6, 1661.0], [54.7, 1662.0], [54.8, 1666.0], [54.9, 1667.0], [55.0, 1673.0], [55.1, 1678.0], [55.2, 1679.0], [55.3, 1682.0], [55.4, 1684.0], [55.5, 1688.0], [55.6, 1689.0], [55.7, 1690.0], [55.8, 1693.0], [55.9, 1698.0], [56.0, 1699.0], [56.1, 1706.0], [56.2, 1707.0], [56.3, 1715.0], [56.4, 1718.0], [56.5, 1718.0], [56.6, 1718.0], [56.7, 1719.0], [56.8, 1719.0], [56.9, 1719.0], [57.0, 1725.0], [57.1, 1725.0], [57.2, 1726.0], [57.3, 1726.0], [57.4, 1727.0], [57.5, 1733.0], [57.6, 1738.0], [57.7, 1740.0], [57.8, 1744.0], [57.9, 1749.0], [58.0, 1755.0], [58.1, 1756.0], [58.2, 1758.0], [58.3, 1760.0], [58.4, 1765.0], [58.5, 1771.0], [58.6, 1772.0], [58.7, 1776.0], [58.8, 1779.0], [58.9, 1789.0], [59.0, 1793.0], [59.1, 1794.0], [59.2, 1795.0], [59.3, 1797.0], [59.4, 1799.0], [59.5, 1806.0], [59.6, 1807.0], [59.7, 1808.0], [59.8, 1808.0], [59.9, 1809.0], [60.0, 1810.0], [60.1, 1819.0], [60.2, 1820.0], [60.3, 1823.0], [60.4, 1827.0], [60.5, 1828.0], [60.6, 1829.0], [60.7, 1829.0], [60.8, 1831.0], [60.9, 1833.0], [61.0, 1834.0], [61.1, 1837.0], [61.2, 1841.0], [61.3, 1842.0], [61.4, 1848.0], [61.5, 1856.0], [61.6, 1858.0], [61.7, 1858.0], [61.8, 1860.0], [61.9, 1861.0], [62.0, 1861.0], [62.1, 1862.0], [62.2, 1863.0], [62.3, 1864.0], [62.4, 1866.0], [62.5, 1867.0], [62.6, 1868.0], [62.7, 1868.0], [62.8, 1868.0], [62.9, 1872.0], [63.0, 1875.0], [63.1, 1879.0], [63.2, 1880.0], [63.3, 1882.0], [63.4, 1887.0], [63.5, 1889.0], [63.6, 1892.0], [63.7, 1892.0], [63.8, 1892.0], [63.9, 1894.0], [64.0, 1895.0], [64.1, 1898.0], [64.2, 1905.0], [64.3, 1907.0], [64.4, 1910.0], [64.5, 1912.0], [64.6, 1915.0], [64.7, 1917.0], [64.8, 1922.0], [64.9, 1926.0], [65.0, 1926.0], [65.1, 1927.0], [65.2, 1928.0], [65.3, 1932.0], [65.4, 1932.0], [65.5, 1935.0], [65.6, 1938.0], [65.7, 1938.0], [65.8, 1938.0], [65.9, 1940.0], [66.0, 1943.0], [66.1, 1946.0], [66.2, 1947.0], [66.3, 1949.0], [66.4, 1952.0], [66.5, 1957.0], [66.6, 1958.0], [66.7, 1958.0], [66.8, 1961.0], [66.9, 1961.0], [67.0, 1962.0], [67.1, 1966.0], [67.2, 1967.0], [67.3, 1968.0], [67.4, 1971.0], [67.5, 1971.0], [67.6, 1979.0], [67.7, 1980.0], [67.8, 1987.0], [67.9, 1993.0], [68.0, 1995.0], [68.1, 2000.0], [68.2, 2000.0], [68.3, 2002.0], [68.4, 2005.0], [68.5, 2009.0], [68.6, 2011.0], [68.7, 2012.0], [68.8, 2024.0], [68.9, 2027.0], [69.0, 2034.0], [69.1, 2040.0], [69.2, 2043.0], [69.3, 2044.0], [69.4, 2047.0], [69.5, 2047.0], [69.6, 2047.0], [69.7, 2047.0], [69.8, 2049.0], [69.9, 2053.0], [70.0, 2059.0], [70.1, 2059.0], [70.2, 2059.0], [70.3, 2060.0], [70.4, 2068.0], [70.5, 2070.0], [70.6, 2071.0], [70.7, 2079.0], [70.8, 2080.0], [70.9, 2085.0], [71.0, 2086.0], [71.1, 2092.0], [71.2, 2095.0], [71.3, 2096.0], [71.4, 2097.0], [71.5, 2097.0], [71.6, 2101.0], [71.7, 2102.0], [71.8, 2103.0], [71.9, 2107.0], [72.0, 2110.0], [72.1, 2111.0], [72.2, 2113.0], [72.3, 2122.0], [72.4, 2122.0], [72.5, 2123.0], [72.6, 2136.0], [72.7, 2137.0], [72.8, 2139.0], [72.9, 2139.0], [73.0, 2145.0], [73.1, 2145.0], [73.2, 2147.0], [73.3, 2148.0], [73.4, 2153.0], [73.5, 2156.0], [73.6, 2160.0], [73.7, 2164.0], [73.8, 2167.0], [73.9, 2168.0], [74.0, 2170.0], [74.1, 2178.0], [74.2, 2178.0], [74.3, 2180.0], [74.4, 2182.0], [74.5, 2182.0], [74.6, 2185.0], [74.7, 2190.0], [74.8, 2193.0], [74.9, 2195.0], [75.0, 2197.0], [75.1, 2205.0], [75.2, 2206.0], [75.3, 2206.0], [75.4, 2206.0], [75.5, 2213.0], [75.6, 2213.0], [75.7, 2214.0], [75.8, 2215.0], [75.9, 2218.0], [76.0, 2220.0], [76.1, 2222.0], [76.2, 2226.0], [76.3, 2228.0], [76.4, 2231.0], [76.5, 2234.0], [76.6, 2238.0], [76.7, 2238.0], [76.8, 2239.0], [76.9, 2247.0], [77.0, 2250.0], [77.1, 2253.0], [77.2, 2256.0], [77.3, 2259.0], [77.4, 2262.0], [77.5, 2266.0], [77.6, 2266.0], [77.7, 2267.0], [77.8, 2268.0], [77.9, 2268.0], [78.0, 2270.0], [78.1, 2276.0], [78.2, 2278.0], [78.3, 2281.0], [78.4, 2287.0], [78.5, 2289.0], [78.6, 2290.0], [78.7, 2296.0], [78.8, 2298.0], [78.9, 2302.0], [79.0, 2302.0], [79.1, 2304.0], [79.2, 2304.0], [79.3, 2304.0], [79.4, 2312.0], [79.5, 2318.0], [79.6, 2318.0], [79.7, 2318.0], [79.8, 2320.0], [79.9, 2326.0], [80.0, 2332.0], [80.1, 2334.0], [80.2, 2335.0], [80.3, 2337.0], [80.4, 2338.0], [80.5, 2347.0], [80.6, 2348.0], [80.7, 2348.0], [80.8, 2357.0], [80.9, 2360.0], [81.0, 2362.0], [81.1, 2369.0], [81.2, 2376.0], [81.3, 2378.0], [81.4, 2382.0], [81.5, 2393.0], [81.6, 2394.0], [81.7, 2395.0], [81.8, 2398.0], [81.9, 2400.0], [82.0, 2401.0], [82.1, 2405.0], [82.2, 2406.0], [82.3, 2415.0], [82.4, 2418.0], [82.5, 2419.0], [82.6, 2424.0], [82.7, 2436.0], [82.8, 2436.0], [82.9, 2439.0], [83.0, 2443.0], [83.1, 2456.0], [83.2, 2458.0], [83.3, 2460.0], [83.4, 2462.0], [83.5, 2462.0], [83.6, 2467.0], [83.7, 2468.0], [83.8, 2470.0], [83.9, 2482.0], [84.0, 2486.0], [84.1, 2490.0], [84.2, 2495.0], [84.3, 2503.0], [84.4, 2504.0], [84.5, 2507.0], [84.6, 2507.0], [84.7, 2514.0], [84.8, 2517.0], [84.9, 2520.0], [85.0, 2521.0], [85.1, 2525.0], [85.2, 2526.0], [85.3, 2530.0], [85.4, 2535.0], [85.5, 2536.0], [85.6, 2540.0], [85.7, 2550.0], [85.8, 2558.0], [85.9, 2559.0], [86.0, 2561.0], [86.1, 2566.0], [86.2, 2570.0], [86.3, 2579.0], [86.4, 2582.0], [86.5, 2582.0], [86.6, 2586.0], [86.7, 2587.0], [86.8, 2591.0], [86.9, 2594.0], [87.0, 2596.0], [87.1, 2596.0], [87.2, 2599.0], [87.3, 2600.0], [87.4, 2601.0], [87.5, 2607.0], [87.6, 2607.0], [87.7, 2609.0], [87.8, 2611.0], [87.9, 2613.0], [88.0, 2616.0], [88.1, 2628.0], [88.2, 2635.0], [88.3, 2635.0], [88.4, 2659.0], [88.5, 2677.0], [88.6, 2684.0], [88.7, 2685.0], [88.8, 2692.0], [88.9, 2692.0], [89.0, 2699.0], [89.1, 2702.0], [89.2, 2711.0], [89.3, 2713.0], [89.4, 2715.0], [89.5, 2720.0], [89.6, 2727.0], [89.7, 2727.0], [89.8, 2747.0], [89.9, 2758.0], [90.0, 2761.0], [90.1, 2762.0], [90.2, 2769.0], [90.3, 2775.0], [90.4, 2784.0], [90.5, 2784.0], [90.6, 2794.0], [90.7, 2800.0], [90.8, 2800.0], [90.9, 2801.0], [91.0, 2817.0], [91.1, 2819.0], [91.2, 2819.0], [91.3, 2820.0], [91.4, 2834.0], [91.5, 2844.0], [91.6, 2848.0], [91.7, 2849.0], [91.8, 2850.0], [91.9, 2853.0], [92.0, 2865.0], [92.1, 2867.0], [92.2, 2872.0], [92.3, 2887.0], [92.4, 2890.0], [92.5, 2893.0], [92.6, 2896.0], [92.7, 2906.0], [92.8, 2906.0], [92.9, 2907.0], [93.0, 2910.0], [93.1, 2910.0], [93.2, 2925.0], [93.3, 2926.0], [93.4, 2934.0], [93.5, 2938.0], [93.6, 2939.0], [93.7, 2941.0], [93.8, 2945.0], [93.9, 2948.0], [94.0, 2949.0], [94.1, 2971.0], [94.2, 2986.0], [94.3, 2988.0], [94.4, 2994.0], [94.5, 2994.0], [94.6, 3013.0], [94.7, 3028.0], [94.8, 3040.0], [94.9, 3052.0], [95.0, 3057.0], [95.1, 3058.0], [95.2, 3061.0], [95.3, 3064.0], [95.4, 3066.0], [95.5, 3066.0], [95.6, 3070.0], [95.7, 3084.0], [95.8, 3101.0], [95.9, 3123.0], [96.0, 3138.0], [96.1, 3141.0], [96.2, 3149.0], [96.3, 3159.0], [96.4, 3161.0], [96.5, 3172.0], [96.6, 3178.0], [96.7, 3184.0], [96.8, 3203.0], [96.9, 3235.0], [97.0, 3243.0], [97.1, 3245.0], [97.2, 3304.0], [97.3, 3306.0], [97.4, 3308.0], [97.5, 3317.0], [97.6, 3327.0], [97.7, 3340.0], [97.8, 3410.0], [97.9, 3414.0], [98.0, 3453.0], [98.1, 3456.0], [98.2, 3457.0], [98.3, 3489.0], [98.4, 3506.0], [98.5, 3510.0], [98.6, 3536.0], [98.7, 3537.0], [98.8, 3546.0], [98.9, 3602.0], [99.0, 3634.0], [99.1, 3710.0], [99.2, 3723.0], [99.3, 3785.0], [99.4, 3902.0], [99.5, 3908.0], [99.6, 3910.0], [99.7, 3915.0], [99.8, 3995.0], [99.9, 3996.0]], "isOverall": false, "label": "POST login", "isController": false}], "supportsControllersDiscrimination": true, "maxX": 100.0, "title": "Response Time Percentiles"}},
        getOptions: function() {
            return {
                series: {
                    points: { show: false }
                },
                legend: {
                    noColumns: 2,
                    show: true,
                    container: '#legendResponseTimePercentiles'
                },
                xaxis: {
                    tickDecimals: 1,
                    axisLabel: "Percentiles",
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                yaxis: {
                    axisLabel: "Percentile value in ms",
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20
                },
                grid: {
                    hoverable: true // IMPORTANT! this is needed for tooltip to
                                    // work
                },
                tooltip: true,
                tooltipOpts: {
                    content: "%s : %x.2 percentile was %y ms"
                },
                selection: { mode: "xy" },
            };
        },
        createGraph: function() {
            var data = this.data;
            var dataset = prepareData(data.result.series, $("#choicesResponseTimePercentiles"));
            var options = this.getOptions();
            prepareOptions(options, data);
            $.plot($("#flotResponseTimesPercentiles"), dataset, options);
            // setup overview
            $.plot($("#overviewResponseTimesPercentiles"), dataset, prepareOverviewOptions(options));
        }
};

/**
 * @param elementId Id of element where we display message
 */
function setEmptyGraph(elementId) {
    $(function() {
        $(elementId).text("No graph series with filter="+seriesFilter);
    });
}

// Response times percentiles
function refreshResponseTimePercentiles() {
    var infos = responseTimePercentilesInfos;
    prepareSeries(infos.data);
    if(infos.data.result.series.length == 0) {
        setEmptyGraph("#bodyResponseTimePercentiles");
        return;
    }
    if (isGraph($("#flotResponseTimesPercentiles"))){
        infos.createGraph();
    } else {
        var choiceContainer = $("#choicesResponseTimePercentiles");
        createLegend(choiceContainer, infos);
        infos.createGraph();
        setGraphZoomable("#flotResponseTimesPercentiles", "#overviewResponseTimesPercentiles");
        $('#bodyResponseTimePercentiles .legendColorBox > div').each(function(i){
            $(this).clone().prependTo(choiceContainer.find("li").eq(i));
        });
    }
}

var responseTimeDistributionInfos = {
        data: {"result": {"minY": 2.0, "minX": 100.0, "maxY": 49.0, "series": [{"data": [[600.0, 35.0], [700.0, 32.0], [800.0, 26.0], [900.0, 36.0], [1000.0, 43.0], [1100.0, 35.0], [1200.0, 37.0], [1300.0, 29.0], [1400.0, 40.0], [1500.0, 49.0], [100.0, 10.0], [1600.0, 37.0], [1700.0, 34.0], [1800.0, 47.0], [1900.0, 39.0], [2000.0, 35.0], [2100.0, 35.0], [2200.0, 39.0], [2300.0, 30.0], [2400.0, 24.0], [2500.0, 30.0], [2600.0, 18.0], [2700.0, 16.0], [2800.0, 20.0], [2900.0, 19.0], [3000.0, 12.0], [3100.0, 10.0], [200.0, 34.0], [3200.0, 4.0], [3300.0, 6.0], [3400.0, 6.0], [3500.0, 5.0], [3700.0, 3.0], [3600.0, 2.0], [3900.0, 6.0], [300.0, 41.0], [400.0, 34.0], [500.0, 42.0]], "isOverall": false, "label": "POST login", "isController": false}], "supportsControllersDiscrimination": true, "granularity": 100, "maxX": 3900.0, "title": "Response Time Distribution"}},
        getOptions: function() {
            var granularity = this.data.result.granularity;
            return {
                legend: {
                    noColumns: 2,
                    show: true,
                    container: '#legendResponseTimeDistribution'
                },
                xaxis:{
                    axisLabel: "Response times in ms",
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                yaxis: {
                    axisLabel: "Number of responses",
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                bars : {
                    show: true,
                    barWidth: this.data.result.granularity
                },
                grid: {
                    hoverable: true // IMPORTANT! this is needed for tooltip to
                                    // work
                },
                tooltip: true,
                tooltipOpts: {
                    content: function(label, xval, yval, flotItem){
                        return yval + " responses for " + label + " were between " + xval + " and " + (xval + granularity) + " ms";
                    }
                }
            };
        },
        createGraph: function() {
            var data = this.data;
            var options = this.getOptions();
            prepareOptions(options, data);
            $.plot($("#flotResponseTimeDistribution"), prepareData(data.result.series, $("#choicesResponseTimeDistribution")), options);
        }

};

// Response time distribution
function refreshResponseTimeDistribution() {
    var infos = responseTimeDistributionInfos;
    prepareSeries(infos.data);
    if(infos.data.result.series.length == 0) {
        setEmptyGraph("#bodyResponseTimeDistribution");
        return;
    }
    if (isGraph($("#flotResponseTimeDistribution"))){
        infos.createGraph();
    }else{
        var choiceContainer = $("#choicesResponseTimeDistribution");
        createLegend(choiceContainer, infos);
        infos.createGraph();
        $('#footerResponseTimeDistribution .legendColorBox > div').each(function(i){
            $(this).clone().prependTo(choiceContainer.find("li").eq(i));
        });
    }
};


var syntheticResponseTimeDistributionInfos = {
        data: {"result": {"minY": 120.0, "minX": 0.0, "ticks": [[0, "Requests having \nresponse time <= 500ms"], [1, "Requests having \nresponse time > 500ms and <= 1,500ms"], [2, "Requests having \nresponse time > 1,500ms"], [3, "Requests in error"]], "maxY": 524.0, "series": [{"data": [[0.0, 120.0]], "color": "#9ACD32", "isOverall": false, "label": "Requests having \nresponse time <= 500ms", "isController": false}, {"data": [[1.0, 356.0]], "color": "yellow", "isOverall": false, "label": "Requests having \nresponse time > 500ms and <= 1,500ms", "isController": false}, {"data": [[2.0, 524.0]], "color": "orange", "isOverall": false, "label": "Requests having \nresponse time > 1,500ms", "isController": false}, {"data": [], "color": "#FF6347", "isOverall": false, "label": "Requests in error", "isController": false}], "supportsControllersDiscrimination": false, "maxX": 2.0, "title": "Synthetic Response Times Distribution"}},
        getOptions: function() {
            return {
                legend: {
                    noColumns: 2,
                    show: true,
                    container: '#legendSyntheticResponseTimeDistribution'
                },
                xaxis:{
                    axisLabel: "Response times ranges",
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                    tickLength:0,
                    min:-0.5,
                    max:3.5
                },
                yaxis: {
                    axisLabel: "Number of responses",
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                bars : {
                    show: true,
                    align: "center",
                    barWidth: 0.25,
                    fill:.75
                },
                grid: {
                    hoverable: true // IMPORTANT! this is needed for tooltip to
                                    // work
                },
                tooltip: true,
                tooltipOpts: {
                    content: function(label, xval, yval, flotItem){
                        return yval + " " + label;
                    }
                }
            };
        },
        createGraph: function() {
            var data = this.data;
            var options = this.getOptions();
            prepareOptions(options, data);
            options.xaxis.ticks = data.result.ticks;
            $.plot($("#flotSyntheticResponseTimeDistribution"), prepareData(data.result.series, $("#choicesSyntheticResponseTimeDistribution")), options);
        }

};

// Response time distribution
function refreshSyntheticResponseTimeDistribution() {
    var infos = syntheticResponseTimeDistributionInfos;
    prepareSeries(infos.data, true);
    if (isGraph($("#flotSyntheticResponseTimeDistribution"))){
        infos.createGraph();
    }else{
        var choiceContainer = $("#choicesSyntheticResponseTimeDistribution");
        createLegend(choiceContainer, infos);
        infos.createGraph();
        $('#footerSyntheticResponseTimeDistribution .legendColorBox > div').each(function(i){
            $(this).clone().prependTo(choiceContainer.find("li").eq(i));
        });
    }
};

var activeThreadsOverTimeInfos = {
        data: {"result": {"minY": 96.79900000000002, "minX": 1.79485932E12, "maxY": 96.79900000000002, "series": [{"data": [[1.79485932E12, 96.79900000000002]], "isOverall": false, "label": "E5 - Estrés: saturación progresiva de login", "isController": false}], "supportsControllersDiscrimination": false, "granularity": 60000, "maxX": 1.79485932E12, "title": "Active Threads Over Time"}},
        getOptions: function() {
            return {
                series: {
                    stack: true,
                    lines: {
                        show: true,
                        fill: true
                    },
                    points: {
                        show: true
                    }
                },
                xaxis: {
                    mode: "time",
                    timeformat: getTimeFormat(this.data.result.granularity),
                    axisLabel: getElapsedTimeLabel(this.data.result.granularity),
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                yaxis: {
                    axisLabel: "Number of active threads",
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20
                },
                legend: {
                    noColumns: 6,
                    show: true,
                    container: '#legendActiveThreadsOverTime'
                },
                grid: {
                    hoverable: true // IMPORTANT! this is needed for tooltip to
                                    // work
                },
                selection: {
                    mode: 'xy'
                },
                tooltip: true,
                tooltipOpts: {
                    content: "%s : At %x there were %y active threads"
                }
            };
        },
        createGraph: function() {
            var data = this.data;
            var dataset = prepareData(data.result.series, $("#choicesActiveThreadsOverTime"));
            var options = this.getOptions();
            prepareOptions(options, data);
            $.plot($("#flotActiveThreadsOverTime"), dataset, options);
            // setup overview
            $.plot($("#overviewActiveThreadsOverTime"), dataset, prepareOverviewOptions(options));
        }
};

// Active Threads Over Time
function refreshActiveThreadsOverTime(fixTimestamps) {
    var infos = activeThreadsOverTimeInfos;
    prepareSeries(infos.data);
    if(fixTimestamps) {
        fixTimeStamps(infos.data.result.series, -18000000);
    }
    if(isGraph($("#flotActiveThreadsOverTime"))) {
        infos.createGraph();
    }else{
        var choiceContainer = $("#choicesActiveThreadsOverTime");
        createLegend(choiceContainer, infos);
        infos.createGraph();
        setGraphZoomable("#flotActiveThreadsOverTime", "#overviewActiveThreadsOverTime");
        $('#footerActiveThreadsOverTime .legendColorBox > div').each(function(i){
            $(this).clone().prependTo(choiceContainer.find("li").eq(i));
        });
    }
};

var timeVsThreadsInfos = {
        data: {"result": {"minY": 196.0, "minX": 1.0, "maxY": 2844.571428571429, "series": [{"data": [[4.0, 272.6666666666667], [5.0, 307.0], [7.0, 306.0], [8.0, 425.0], [9.0, 584.0], [10.0, 212.0], [11.0, 289.33333333333337], [12.0, 318.0], [13.0, 196.0], [14.0, 557.0], [15.0, 299.0], [16.0, 217.5], [17.0, 345.75], [18.0, 285.5], [19.0, 492.0], [20.0, 443.0], [21.0, 309.5], [22.0, 353.75], [23.0, 552.3333333333334], [24.0, 370.5], [25.0, 479.0], [26.0, 285.0], [27.0, 739.6666666666666], [28.0, 753.3333333333334], [29.0, 564.25], [30.0, 565.0], [31.0, 559.0], [32.0, 512.0], [33.0, 620.75], [34.0, 699.3333333333333], [35.0, 876.6666666666666], [36.0, 735.75], [37.0, 619.5], [38.0, 783.7777777777778], [39.0, 747.6666666666667], [40.0, 800.3333333333333], [41.0, 515.0], [42.0, 603.6], [43.0, 732.8333333333334], [44.0, 574.5], [45.0, 727.8], [46.0, 798.8888888888889], [47.0, 778.4], [48.0, 1078.5], [49.0, 655.0], [50.0, 952.5], [51.0, 993.6666666666667], [52.0, 854.2222222222222], [53.0, 1552.0], [55.0, 943.9999999999999], [54.0, 879.7142857142857], [56.0, 911.3636363636363], [57.0, 739.8000000000001], [58.0, 797.5714285714286], [59.0, 1221.0], [60.0, 256.0], [61.0, 1445.5], [62.0, 1297.8], [63.0, 960.3333333333333], [64.0, 1368.2], [66.0, 1423.5], [67.0, 984.6666666666666], [65.0, 549.0], [68.0, 1377.25], [69.0, 772.0], [70.0, 1322.6666666666667], [71.0, 1152.5], [72.0, 1276.3846153846152], [74.0, 1765.5], [73.0, 1762.8333333333333], [75.0, 1383.0], [76.0, 1789.3333333333333], [77.0, 1699.0], [78.0, 1383.0666666666668], [79.0, 1435.1], [81.0, 1225.4285714285713], [83.0, 2060.0], [82.0, 1880.7499999999998], [80.0, 1509.375], [84.0, 2131.3333333333335], [85.0, 1178.5], [86.0, 1249.5], [87.0, 1984.5555555555557], [90.0, 1195.8333333333333], [91.0, 1452.3333333333333], [89.0, 2014.0], [93.0, 1830.6], [94.0, 540.4], [95.0, 1428.3333333333333], [92.0, 1271.5], [97.0, 1305.0], [98.0, 1469.4999999999998], [99.0, 1661.0], [96.0, 1588.0], [100.0, 1703.75], [101.0, 1473.875], [102.0, 1599.5], [103.0, 1366.6], [104.0, 1867.0], [105.0, 1617.0], [106.0, 2094.0], [107.0, 1503.888888888889], [108.0, 1762.3999999999999], [109.0, 1924.0], [110.0, 1810.928571428571], [111.0, 1647.2857142857142], [112.0, 1804.8], [114.0, 2260.0], [115.0, 1801.111111111111], [113.0, 1564.3333333333333], [116.0, 1946.5333333333335], [117.0, 1490.2142857142858], [118.0, 1481.9166666666667], [119.0, 1892.6428571428573], [120.0, 1688.8888888888891], [121.0, 1447.0], [123.0, 1986.8749999999998], [122.0, 1924.6666666666665], [124.0, 1752.9354838709683], [125.0, 1902.4285714285716], [126.0, 1932.8750000000002], [127.0, 2115.25], [128.0, 2269.875], [129.0, 2269.8749999999995], [131.0, 1968.3333333333335], [132.0, 2205.5], [134.0, 2010.7142857142858], [133.0, 2015.3333333333333], [135.0, 2111.5714285714284], [130.0, 2022.0], [136.0, 2103.5], [137.0, 1847.0], [138.0, 2732.8461538461534], [139.0, 2413.0], [140.0, 1974.9999999999998], [141.0, 2227.6428571428564], [142.0, 2844.571428571429], [143.0, 2826.7499999999995], [144.0, 2103.3636363636365], [145.0, 2165.92], [146.0, 2351.3846153846152], [147.0, 2578.3846153846152], [148.0, 2439.2666666666664], [149.0, 2322.7], [151.0, 2116.705882352941], [150.0, 2214.0], [152.0, 2150.4375], [153.0, 2206.0], [1.0, 325.0]], "isOverall": false, "label": "POST login", "isController": false}, {"data": [[96.79900000000002, 1579.8610000000006]], "isOverall": false, "label": "POST login-Aggregated", "isController": false}], "supportsControllersDiscrimination": true, "maxX": 153.0, "title": "Time VS Threads"}},
        getOptions: function() {
            return {
                series: {
                    lines: {
                        show: true
                    },
                    points: {
                        show: true
                    }
                },
                xaxis: {
                    axisLabel: "Number of active threads",
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                yaxis: {
                    axisLabel: "Average response times in ms",
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20
                },
                legend: { noColumns: 2,show: true, container: '#legendTimeVsThreads' },
                selection: {
                    mode: 'xy'
                },
                grid: {
                    hoverable: true // IMPORTANT! this is needed for tooltip to work
                },
                tooltip: true,
                tooltipOpts: {
                    content: "%s: At %x.2 active threads, Average response time was %y.2 ms"
                }
            };
        },
        createGraph: function() {
            var data = this.data;
            var dataset = prepareData(data.result.series, $("#choicesTimeVsThreads"));
            var options = this.getOptions();
            prepareOptions(options, data);
            $.plot($("#flotTimesVsThreads"), dataset, options);
            // setup overview
            $.plot($("#overviewTimesVsThreads"), dataset, prepareOverviewOptions(options));
        }
};

// Time vs threads
function refreshTimeVsThreads(){
    var infos = timeVsThreadsInfos;
    prepareSeries(infos.data);
    if(infos.data.result.series.length == 0) {
        setEmptyGraph("#bodyTimeVsThreads");
        return;
    }
    if(isGraph($("#flotTimesVsThreads"))){
        infos.createGraph();
    }else{
        var choiceContainer = $("#choicesTimeVsThreads");
        createLegend(choiceContainer, infos);
        infos.createGraph();
        setGraphZoomable("#flotTimesVsThreads", "#overviewTimesVsThreads");
        $('#footerTimeVsThreads .legendColorBox > div').each(function(i){
            $(this).clone().prependTo(choiceContainer.find("li").eq(i));
        });
    }
};

var bytesThroughputOverTimeInfos = {
        data : {"result": {"minY": 4613.666666666667, "minX": 1.79485932E12, "maxY": 12727.666666666666, "series": [{"data": [[1.79485932E12, 12727.666666666666]], "isOverall": false, "label": "Bytes received per second", "isController": false}, {"data": [[1.79485932E12, 4613.666666666667]], "isOverall": false, "label": "Bytes sent per second", "isController": false}], "supportsControllersDiscrimination": false, "granularity": 60000, "maxX": 1.79485932E12, "title": "Bytes Throughput Over Time"}},
        getOptions : function(){
            return {
                series: {
                    lines: {
                        show: true
                    },
                    points: {
                        show: true
                    }
                },
                xaxis: {
                    mode: "time",
                    timeformat: getTimeFormat(this.data.result.granularity),
                    axisLabel: getElapsedTimeLabel(this.data.result.granularity) ,
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                yaxis: {
                    axisLabel: "Bytes / sec",
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                legend: {
                    noColumns: 2,
                    show: true,
                    container: '#legendBytesThroughputOverTime'
                },
                selection: {
                    mode: "xy"
                },
                grid: {
                    hoverable: true // IMPORTANT! this is needed for tooltip to
                                    // work
                },
                tooltip: true,
                tooltipOpts: {
                    content: "%s at %x was %y"
                }
            };
        },
        createGraph : function() {
            var data = this.data;
            var dataset = prepareData(data.result.series, $("#choicesBytesThroughputOverTime"));
            var options = this.getOptions();
            prepareOptions(options, data);
            $.plot($("#flotBytesThroughputOverTime"), dataset, options);
            // setup overview
            $.plot($("#overviewBytesThroughputOverTime"), dataset, prepareOverviewOptions(options));
        }
};

// Bytes throughput Over Time
function refreshBytesThroughputOverTime(fixTimestamps) {
    var infos = bytesThroughputOverTimeInfos;
    prepareSeries(infos.data);
    if(fixTimestamps) {
        fixTimeStamps(infos.data.result.series, -18000000);
    }
    if(isGraph($("#flotBytesThroughputOverTime"))){
        infos.createGraph();
    }else{
        var choiceContainer = $("#choicesBytesThroughputOverTime");
        createLegend(choiceContainer, infos);
        infos.createGraph();
        setGraphZoomable("#flotBytesThroughputOverTime", "#overviewBytesThroughputOverTime");
        $('#footerBytesThroughputOverTime .legendColorBox > div').each(function(i){
            $(this).clone().prependTo(choiceContainer.find("li").eq(i));
        });
    }
}

var responseTimesOverTimeInfos = {
        data: {"result": {"minY": 1579.8610000000006, "minX": 1.79485932E12, "maxY": 1579.8610000000006, "series": [{"data": [[1.79485932E12, 1579.8610000000006]], "isOverall": false, "label": "POST login", "isController": false}], "supportsControllersDiscrimination": true, "granularity": 60000, "maxX": 1.79485932E12, "title": "Response Time Over Time"}},
        getOptions: function(){
            return {
                series: {
                    lines: {
                        show: true
                    },
                    points: {
                        show: true
                    }
                },
                xaxis: {
                    mode: "time",
                    timeformat: getTimeFormat(this.data.result.granularity),
                    axisLabel: getElapsedTimeLabel(this.data.result.granularity),
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                yaxis: {
                    axisLabel: "Average response time in ms",
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                legend: {
                    noColumns: 2,
                    show: true,
                    container: '#legendResponseTimesOverTime'
                },
                selection: {
                    mode: 'xy'
                },
                grid: {
                    hoverable: true // IMPORTANT! this is needed for tooltip to
                                    // work
                },
                tooltip: true,
                tooltipOpts: {
                    content: "%s : at %x Average response time was %y ms"
                }
            };
        },
        createGraph: function() {
            var data = this.data;
            var dataset = prepareData(data.result.series, $("#choicesResponseTimesOverTime"));
            var options = this.getOptions();
            prepareOptions(options, data);
            $.plot($("#flotResponseTimesOverTime"), dataset, options);
            // setup overview
            $.plot($("#overviewResponseTimesOverTime"), dataset, prepareOverviewOptions(options));
        }
};

// Response Times Over Time
function refreshResponseTimeOverTime(fixTimestamps) {
    var infos = responseTimesOverTimeInfos;
    prepareSeries(infos.data);
    if(infos.data.result.series.length == 0) {
        setEmptyGraph("#bodyResponseTimeOverTime");
        return;
    }
    if(fixTimestamps) {
        fixTimeStamps(infos.data.result.series, -18000000);
    }
    if(isGraph($("#flotResponseTimesOverTime"))){
        infos.createGraph();
    }else{
        var choiceContainer = $("#choicesResponseTimesOverTime");
        createLegend(choiceContainer, infos);
        infos.createGraph();
        setGraphZoomable("#flotResponseTimesOverTime", "#overviewResponseTimesOverTime");
        $('#footerResponseTimesOverTime .legendColorBox > div').each(function(i){
            $(this).clone().prependTo(choiceContainer.find("li").eq(i));
        });
    }
};

var latenciesOverTimeInfos = {
        data: {"result": {"minY": 1579.8100000000018, "minX": 1.79485932E12, "maxY": 1579.8100000000018, "series": [{"data": [[1.79485932E12, 1579.8100000000018]], "isOverall": false, "label": "POST login", "isController": false}], "supportsControllersDiscrimination": true, "granularity": 60000, "maxX": 1.79485932E12, "title": "Latencies Over Time"}},
        getOptions: function() {
            return {
                series: {
                    lines: {
                        show: true
                    },
                    points: {
                        show: true
                    }
                },
                xaxis: {
                    mode: "time",
                    timeformat: getTimeFormat(this.data.result.granularity),
                    axisLabel: getElapsedTimeLabel(this.data.result.granularity),
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                yaxis: {
                    axisLabel: "Average response latencies in ms",
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                legend: {
                    noColumns: 2,
                    show: true,
                    container: '#legendLatenciesOverTime'
                },
                selection: {
                    mode: 'xy'
                },
                grid: {
                    hoverable: true // IMPORTANT! this is needed for tooltip to
                                    // work
                },
                tooltip: true,
                tooltipOpts: {
                    content: "%s : at %x Average latency was %y ms"
                }
            };
        },
        createGraph: function () {
            var data = this.data;
            var dataset = prepareData(data.result.series, $("#choicesLatenciesOverTime"));
            var options = this.getOptions();
            prepareOptions(options, data);
            $.plot($("#flotLatenciesOverTime"), dataset, options);
            // setup overview
            $.plot($("#overviewLatenciesOverTime"), dataset, prepareOverviewOptions(options));
        }
};

// Latencies Over Time
function refreshLatenciesOverTime(fixTimestamps) {
    var infos = latenciesOverTimeInfos;
    prepareSeries(infos.data);
    if(infos.data.result.series.length == 0) {
        setEmptyGraph("#bodyLatenciesOverTime");
        return;
    }
    if(fixTimestamps) {
        fixTimeStamps(infos.data.result.series, -18000000);
    }
    if(isGraph($("#flotLatenciesOverTime"))) {
        infos.createGraph();
    }else {
        var choiceContainer = $("#choicesLatenciesOverTime");
        createLegend(choiceContainer, infos);
        infos.createGraph();
        setGraphZoomable("#flotLatenciesOverTime", "#overviewLatenciesOverTime");
        $('#footerLatenciesOverTime .legendColorBox > div').each(function(i){
            $(this).clone().prependTo(choiceContainer.find("li").eq(i));
        });
    }
};

var connectTimeOverTimeInfos = {
        data: {"result": {"minY": 1.2270000000000003, "minX": 1.79485932E12, "maxY": 1.2270000000000003, "series": [{"data": [[1.79485932E12, 1.2270000000000003]], "isOverall": false, "label": "POST login", "isController": false}], "supportsControllersDiscrimination": true, "granularity": 60000, "maxX": 1.79485932E12, "title": "Connect Time Over Time"}},
        getOptions: function() {
            return {
                series: {
                    lines: {
                        show: true
                    },
                    points: {
                        show: true
                    }
                },
                xaxis: {
                    mode: "time",
                    timeformat: getTimeFormat(this.data.result.granularity),
                    axisLabel: getConnectTimeLabel(this.data.result.granularity),
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                yaxis: {
                    axisLabel: "Average Connect Time in ms",
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                legend: {
                    noColumns: 2,
                    show: true,
                    container: '#legendConnectTimeOverTime'
                },
                selection: {
                    mode: 'xy'
                },
                grid: {
                    hoverable: true // IMPORTANT! this is needed for tooltip to
                                    // work
                },
                tooltip: true,
                tooltipOpts: {
                    content: "%s : at %x Average connect time was %y ms"
                }
            };
        },
        createGraph: function () {
            var data = this.data;
            var dataset = prepareData(data.result.series, $("#choicesConnectTimeOverTime"));
            var options = this.getOptions();
            prepareOptions(options, data);
            $.plot($("#flotConnectTimeOverTime"), dataset, options);
            // setup overview
            $.plot($("#overviewConnectTimeOverTime"), dataset, prepareOverviewOptions(options));
        }
};

// Connect Time Over Time
function refreshConnectTimeOverTime(fixTimestamps) {
    var infos = connectTimeOverTimeInfos;
    prepareSeries(infos.data);
    if(infos.data.result.series.length == 0) {
        setEmptyGraph("#bodyConnectTimeOverTime");
        return;
    }
    if(fixTimestamps) {
        fixTimeStamps(infos.data.result.series, -18000000);
    }
    if(isGraph($("#flotConnectTimeOverTime"))) {
        infos.createGraph();
    }else {
        var choiceContainer = $("#choicesConnectTimeOverTime");
        createLegend(choiceContainer, infos);
        infos.createGraph();
        setGraphZoomable("#flotConnectTimeOverTime", "#overviewConnectTimeOverTime");
        $('#footerConnectTimeOverTime .legendColorBox > div').each(function(i){
            $(this).clone().prependTo(choiceContainer.find("li").eq(i));
        });
    }
};

var responseTimePercentilesOverTimeInfos = {
        data: {"result": {"minY": 122.0, "minX": 1.79485932E12, "maxY": 3996.0, "series": [{"data": [[1.79485932E12, 3996.0]], "isOverall": false, "label": "Max", "isController": false}, {"data": [[1.79485932E12, 122.0]], "isOverall": false, "label": "Min", "isController": false}, {"data": [[1.79485932E12, 2760.7]], "isOverall": false, "label": "90th percentile", "isController": false}, {"data": [[1.79485932E12, 3633.6800000000003]], "isOverall": false, "label": "99th percentile", "isController": false}, {"data": [[1.79485932E12, 1554.0]], "isOverall": false, "label": "Median", "isController": false}, {"data": [[1.79485932E12, 3056.7499999999995]], "isOverall": false, "label": "95th percentile", "isController": false}], "supportsControllersDiscrimination": false, "granularity": 60000, "maxX": 1.79485932E12, "title": "Response Time Percentiles Over Time (successful requests only)"}},
        getOptions: function() {
            return {
                series: {
                    lines: {
                        show: true,
                        fill: true
                    },
                    points: {
                        show: true
                    }
                },
                xaxis: {
                    mode: "time",
                    timeformat: getTimeFormat(this.data.result.granularity),
                    axisLabel: getElapsedTimeLabel(this.data.result.granularity),
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                yaxis: {
                    axisLabel: "Response Time in ms",
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                legend: {
                    noColumns: 2,
                    show: true,
                    container: '#legendResponseTimePercentilesOverTime'
                },
                selection: {
                    mode: 'xy'
                },
                grid: {
                    hoverable: true // IMPORTANT! this is needed for tooltip to
                                    // work
                },
                tooltip: true,
                tooltipOpts: {
                    content: "%s : at %x Response time was %y ms"
                }
            };
        },
        createGraph: function () {
            var data = this.data;
            var dataset = prepareData(data.result.series, $("#choicesResponseTimePercentilesOverTime"));
            var options = this.getOptions();
            prepareOptions(options, data);
            $.plot($("#flotResponseTimePercentilesOverTime"), dataset, options);
            // setup overview
            $.plot($("#overviewResponseTimePercentilesOverTime"), dataset, prepareOverviewOptions(options));
        }
};

// Response Time Percentiles Over Time
function refreshResponseTimePercentilesOverTime(fixTimestamps) {
    var infos = responseTimePercentilesOverTimeInfos;
    prepareSeries(infos.data);
    if(fixTimestamps) {
        fixTimeStamps(infos.data.result.series, -18000000);
    }
    if(isGraph($("#flotResponseTimePercentilesOverTime"))) {
        infos.createGraph();
    }else {
        var choiceContainer = $("#choicesResponseTimePercentilesOverTime");
        createLegend(choiceContainer, infos);
        infos.createGraph();
        setGraphZoomable("#flotResponseTimePercentilesOverTime", "#overviewResponseTimePercentilesOverTime");
        $('#footerResponseTimePercentilesOverTime .legendColorBox > div').each(function(i){
            $(this).clone().prependTo(choiceContainer.find("li").eq(i));
        });
    }
};


var responseTimeVsRequestInfos = {
    data: {"result": {"minY": 262.0, "minX": 28.0, "maxY": 2467.0, "series": [{"data": [[39.0, 1483.0], [42.0, 1962.5], [44.0, 438.5], [45.0, 696.0], [49.0, 1248.0], [48.0, 2433.5], [57.0, 1247.5], [59.0, 1797.0], [63.0, 2276.0], [68.0, 2145.0], [77.0, 1654.0], [87.0, 2467.0], [84.0, 1718.0], [85.0, 810.0], [28.0, 262.0]], "isOverall": false, "label": "Successes", "isController": false}], "supportsControllersDiscrimination": false, "granularity": 1000, "maxX": 87.0, "title": "Response Time Vs Request"}},
    getOptions: function() {
        return {
            series: {
                lines: {
                    show: false
                },
                points: {
                    show: true
                }
            },
            xaxis: {
                axisLabel: "Global number of requests per second",
                axisLabelUseCanvas: true,
                axisLabelFontSizePixels: 12,
                axisLabelFontFamily: 'Verdana, Arial',
                axisLabelPadding: 20,
            },
            yaxis: {
                axisLabel: "Median Response Time in ms",
                axisLabelUseCanvas: true,
                axisLabelFontSizePixels: 12,
                axisLabelFontFamily: 'Verdana, Arial',
                axisLabelPadding: 20,
            },
            legend: {
                noColumns: 2,
                show: true,
                container: '#legendResponseTimeVsRequest'
            },
            selection: {
                mode: 'xy'
            },
            grid: {
                hoverable: true // IMPORTANT! this is needed for tooltip to work
            },
            tooltip: true,
            tooltipOpts: {
                content: "%s : Median response time at %x req/s was %y ms"
            },
            colors: ["#9ACD32", "#FF6347"]
        };
    },
    createGraph: function () {
        var data = this.data;
        var dataset = prepareData(data.result.series, $("#choicesResponseTimeVsRequest"));
        var options = this.getOptions();
        prepareOptions(options, data);
        $.plot($("#flotResponseTimeVsRequest"), dataset, options);
        // setup overview
        $.plot($("#overviewResponseTimeVsRequest"), dataset, prepareOverviewOptions(options));

    }
};

// Response Time vs Request
function refreshResponseTimeVsRequest() {
    var infos = responseTimeVsRequestInfos;
    prepareSeries(infos.data);
    if (isGraph($("#flotResponseTimeVsRequest"))){
        infos.createGraph();
    }else{
        var choiceContainer = $("#choicesResponseTimeVsRequest");
        createLegend(choiceContainer, infos);
        infos.createGraph();
        setGraphZoomable("#flotResponseTimeVsRequest", "#overviewResponseTimeVsRequest");
        $('#footerResponseRimeVsRequest .legendColorBox > div').each(function(i){
            $(this).clone().prependTo(choiceContainer.find("li").eq(i));
        });
    }
};


var latenciesVsRequestInfos = {
    data: {"result": {"minY": 262.0, "minX": 28.0, "maxY": 2467.0, "series": [{"data": [[39.0, 1483.0], [42.0, 1962.5], [44.0, 438.5], [45.0, 696.0], [49.0, 1248.0], [48.0, 2433.5], [57.0, 1247.5], [59.0, 1797.0], [63.0, 2276.0], [68.0, 2144.5], [77.0, 1654.0], [87.0, 2467.0], [84.0, 1718.0], [85.0, 810.0], [28.0, 262.0]], "isOverall": false, "label": "Successes", "isController": false}], "supportsControllersDiscrimination": false, "granularity": 1000, "maxX": 87.0, "title": "Latencies Vs Request"}},
    getOptions: function() {
        return{
            series: {
                lines: {
                    show: false
                },
                points: {
                    show: true
                }
            },
            xaxis: {
                axisLabel: "Global number of requests per second",
                axisLabelUseCanvas: true,
                axisLabelFontSizePixels: 12,
                axisLabelFontFamily: 'Verdana, Arial',
                axisLabelPadding: 20,
            },
            yaxis: {
                axisLabel: "Median Latency in ms",
                axisLabelUseCanvas: true,
                axisLabelFontSizePixels: 12,
                axisLabelFontFamily: 'Verdana, Arial',
                axisLabelPadding: 20,
            },
            legend: { noColumns: 2,show: true, container: '#legendLatencyVsRequest' },
            selection: {
                mode: 'xy'
            },
            grid: {
                hoverable: true // IMPORTANT! this is needed for tooltip to work
            },
            tooltip: true,
            tooltipOpts: {
                content: "%s : Median Latency time at %x req/s was %y ms"
            },
            colors: ["#9ACD32", "#FF6347"]
        };
    },
    createGraph: function () {
        var data = this.data;
        var dataset = prepareData(data.result.series, $("#choicesLatencyVsRequest"));
        var options = this.getOptions();
        prepareOptions(options, data);
        $.plot($("#flotLatenciesVsRequest"), dataset, options);
        // setup overview
        $.plot($("#overviewLatenciesVsRequest"), dataset, prepareOverviewOptions(options));
    }
};

// Latencies vs Request
function refreshLatenciesVsRequest() {
        var infos = latenciesVsRequestInfos;
        prepareSeries(infos.data);
        if(isGraph($("#flotLatenciesVsRequest"))){
            infos.createGraph();
        }else{
            var choiceContainer = $("#choicesLatencyVsRequest");
            createLegend(choiceContainer, infos);
            infos.createGraph();
            setGraphZoomable("#flotLatenciesVsRequest", "#overviewLatenciesVsRequest");
            $('#footerLatenciesVsRequest .legendColorBox > div').each(function(i){
                $(this).clone().prependTo(choiceContainer.find("li").eq(i));
            });
        }
};

var hitsPerSecondInfos = {
        data: {"result": {"minY": 16.666666666666668, "minX": 1.79485932E12, "maxY": 16.666666666666668, "series": [{"data": [[1.79485932E12, 16.666666666666668]], "isOverall": false, "label": "hitsPerSecond", "isController": false}], "supportsControllersDiscrimination": false, "granularity": 60000, "maxX": 1.79485932E12, "title": "Hits Per Second"}},
        getOptions: function() {
            return {
                series: {
                    lines: {
                        show: true
                    },
                    points: {
                        show: true
                    }
                },
                xaxis: {
                    mode: "time",
                    timeformat: getTimeFormat(this.data.result.granularity),
                    axisLabel: getElapsedTimeLabel(this.data.result.granularity),
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                yaxis: {
                    axisLabel: "Number of hits / sec",
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20
                },
                legend: {
                    noColumns: 2,
                    show: true,
                    container: "#legendHitsPerSecond"
                },
                selection: {
                    mode : 'xy'
                },
                grid: {
                    hoverable: true // IMPORTANT! this is needed for tooltip to
                                    // work
                },
                tooltip: true,
                tooltipOpts: {
                    content: "%s at %x was %y.2 hits/sec"
                }
            };
        },
        createGraph: function createGraph() {
            var data = this.data;
            var dataset = prepareData(data.result.series, $("#choicesHitsPerSecond"));
            var options = this.getOptions();
            prepareOptions(options, data);
            $.plot($("#flotHitsPerSecond"), dataset, options);
            // setup overview
            $.plot($("#overviewHitsPerSecond"), dataset, prepareOverviewOptions(options));
        }
};

// Hits per second
function refreshHitsPerSecond(fixTimestamps) {
    var infos = hitsPerSecondInfos;
    prepareSeries(infos.data);
    if(fixTimestamps) {
        fixTimeStamps(infos.data.result.series, -18000000);
    }
    if (isGraph($("#flotHitsPerSecond"))){
        infos.createGraph();
    }else{
        var choiceContainer = $("#choicesHitsPerSecond");
        createLegend(choiceContainer, infos);
        infos.createGraph();
        setGraphZoomable("#flotHitsPerSecond", "#overviewHitsPerSecond");
        $('#footerHitsPerSecond .legendColorBox > div').each(function(i){
            $(this).clone().prependTo(choiceContainer.find("li").eq(i));
        });
    }
}

var codesPerSecondInfos = {
        data: {"result": {"minY": 16.666666666666668, "minX": 1.79485932E12, "maxY": 16.666666666666668, "series": [{"data": [[1.79485932E12, 16.666666666666668]], "isOverall": false, "label": "200", "isController": false}], "supportsControllersDiscrimination": false, "granularity": 60000, "maxX": 1.79485932E12, "title": "Codes Per Second"}},
        getOptions: function(){
            return {
                series: {
                    lines: {
                        show: true
                    },
                    points: {
                        show: true
                    }
                },
                xaxis: {
                    mode: "time",
                    timeformat: getTimeFormat(this.data.result.granularity),
                    axisLabel: getElapsedTimeLabel(this.data.result.granularity),
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                yaxis: {
                    axisLabel: "Number of responses / sec",
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                legend: {
                    noColumns: 2,
                    show: true,
                    container: "#legendCodesPerSecond"
                },
                selection: {
                    mode: 'xy'
                },
                grid: {
                    hoverable: true // IMPORTANT! this is needed for tooltip to
                                    // work
                },
                tooltip: true,
                tooltipOpts: {
                    content: "Number of Response Codes %s at %x was %y.2 responses / sec"
                }
            };
        },
    createGraph: function() {
        var data = this.data;
        var dataset = prepareData(data.result.series, $("#choicesCodesPerSecond"));
        var options = this.getOptions();
        prepareOptions(options, data);
        $.plot($("#flotCodesPerSecond"), dataset, options);
        // setup overview
        $.plot($("#overviewCodesPerSecond"), dataset, prepareOverviewOptions(options));
    }
};

// Codes per second
function refreshCodesPerSecond(fixTimestamps) {
    var infos = codesPerSecondInfos;
    prepareSeries(infos.data);
    if(fixTimestamps) {
        fixTimeStamps(infos.data.result.series, -18000000);
    }
    if(isGraph($("#flotCodesPerSecond"))){
        infos.createGraph();
    }else{
        var choiceContainer = $("#choicesCodesPerSecond");
        createLegend(choiceContainer, infos);
        infos.createGraph();
        setGraphZoomable("#flotCodesPerSecond", "#overviewCodesPerSecond");
        $('#footerCodesPerSecond .legendColorBox > div').each(function(i){
            $(this).clone().prependTo(choiceContainer.find("li").eq(i));
        });
    }
};

var transactionsPerSecondInfos = {
        data: {"result": {"minY": 16.666666666666668, "minX": 1.79485932E12, "maxY": 16.666666666666668, "series": [{"data": [[1.79485932E12, 16.666666666666668]], "isOverall": false, "label": "POST login-success", "isController": false}], "supportsControllersDiscrimination": true, "granularity": 60000, "maxX": 1.79485932E12, "title": "Transactions Per Second"}},
        getOptions: function(){
            return {
                series: {
                    lines: {
                        show: true
                    },
                    points: {
                        show: true
                    }
                },
                xaxis: {
                    mode: "time",
                    timeformat: getTimeFormat(this.data.result.granularity),
                    axisLabel: getElapsedTimeLabel(this.data.result.granularity),
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                yaxis: {
                    axisLabel: "Number of transactions / sec",
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20
                },
                legend: {
                    noColumns: 2,
                    show: true,
                    container: "#legendTransactionsPerSecond"
                },
                selection: {
                    mode: 'xy'
                },
                grid: {
                    hoverable: true // IMPORTANT! this is needed for tooltip to
                                    // work
                },
                tooltip: true,
                tooltipOpts: {
                    content: "%s at %x was %y transactions / sec"
                }
            };
        },
    createGraph: function () {
        var data = this.data;
        var dataset = prepareData(data.result.series, $("#choicesTransactionsPerSecond"));
        var options = this.getOptions();
        prepareOptions(options, data);
        $.plot($("#flotTransactionsPerSecond"), dataset, options);
        // setup overview
        $.plot($("#overviewTransactionsPerSecond"), dataset, prepareOverviewOptions(options));
    }
};

// Transactions per second
function refreshTransactionsPerSecond(fixTimestamps) {
    var infos = transactionsPerSecondInfos;
    prepareSeries(infos.data);
    if(infos.data.result.series.length == 0) {
        setEmptyGraph("#bodyTransactionsPerSecond");
        return;
    }
    if(fixTimestamps) {
        fixTimeStamps(infos.data.result.series, -18000000);
    }
    if(isGraph($("#flotTransactionsPerSecond"))){
        infos.createGraph();
    }else{
        var choiceContainer = $("#choicesTransactionsPerSecond");
        createLegend(choiceContainer, infos);
        infos.createGraph();
        setGraphZoomable("#flotTransactionsPerSecond", "#overviewTransactionsPerSecond");
        $('#footerTransactionsPerSecond .legendColorBox > div').each(function(i){
            $(this).clone().prependTo(choiceContainer.find("li").eq(i));
        });
    }
};

var totalTPSInfos = {
        data: {"result": {"minY": 16.666666666666668, "minX": 1.79485932E12, "maxY": 16.666666666666668, "series": [{"data": [[1.79485932E12, 16.666666666666668]], "isOverall": false, "label": "Transaction-success", "isController": false}, {"data": [], "isOverall": false, "label": "Transaction-failure", "isController": false}], "supportsControllersDiscrimination": true, "granularity": 60000, "maxX": 1.79485932E12, "title": "Total Transactions Per Second"}},
        getOptions: function(){
            return {
                series: {
                    lines: {
                        show: true
                    },
                    points: {
                        show: true
                    }
                },
                xaxis: {
                    mode: "time",
                    timeformat: getTimeFormat(this.data.result.granularity),
                    axisLabel: getElapsedTimeLabel(this.data.result.granularity),
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                yaxis: {
                    axisLabel: "Number of transactions / sec",
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20
                },
                legend: {
                    noColumns: 2,
                    show: true,
                    container: "#legendTotalTPS"
                },
                selection: {
                    mode: 'xy'
                },
                grid: {
                    hoverable: true // IMPORTANT! this is needed for tooltip to
                                    // work
                },
                tooltip: true,
                tooltipOpts: {
                    content: "%s at %x was %y transactions / sec"
                },
                colors: ["#9ACD32", "#FF6347"]
            };
        },
    createGraph: function () {
        var data = this.data;
        var dataset = prepareData(data.result.series, $("#choicesTotalTPS"));
        var options = this.getOptions();
        prepareOptions(options, data);
        $.plot($("#flotTotalTPS"), dataset, options);
        // setup overview
        $.plot($("#overviewTotalTPS"), dataset, prepareOverviewOptions(options));
    }
};

// Total Transactions per second
function refreshTotalTPS(fixTimestamps) {
    var infos = totalTPSInfos;
    // We want to ignore seriesFilter
    prepareSeries(infos.data, false, true);
    if(fixTimestamps) {
        fixTimeStamps(infos.data.result.series, -18000000);
    }
    if(isGraph($("#flotTotalTPS"))){
        infos.createGraph();
    }else{
        var choiceContainer = $("#choicesTotalTPS");
        createLegend(choiceContainer, infos);
        infos.createGraph();
        setGraphZoomable("#flotTotalTPS", "#overviewTotalTPS");
        $('#footerTotalTPS .legendColorBox > div').each(function(i){
            $(this).clone().prependTo(choiceContainer.find("li").eq(i));
        });
    }
};

// Collapse the graph matching the specified DOM element depending the collapsed
// status
function collapse(elem, collapsed){
    if(collapsed){
        $(elem).parent().find(".fa-chevron-up").removeClass("fa-chevron-up").addClass("fa-chevron-down");
    } else {
        $(elem).parent().find(".fa-chevron-down").removeClass("fa-chevron-down").addClass("fa-chevron-up");
        if (elem.id == "bodyBytesThroughputOverTime") {
            if (isGraph($(elem).find('.flot-chart-content')) == false) {
                refreshBytesThroughputOverTime(true);
            }
            document.location.href="#bytesThroughputOverTime";
        } else if (elem.id == "bodyLatenciesOverTime") {
            if (isGraph($(elem).find('.flot-chart-content')) == false) {
                refreshLatenciesOverTime(true);
            }
            document.location.href="#latenciesOverTime";
        } else if (elem.id == "bodyCustomGraph") {
            if (isGraph($(elem).find('.flot-chart-content')) == false) {
                refreshCustomGraph(true);
            }
            document.location.href="#responseCustomGraph";
        } else if (elem.id == "bodyConnectTimeOverTime") {
            if (isGraph($(elem).find('.flot-chart-content')) == false) {
                refreshConnectTimeOverTime(true);
            }
            document.location.href="#connectTimeOverTime";
        } else if (elem.id == "bodyResponseTimePercentilesOverTime") {
            if (isGraph($(elem).find('.flot-chart-content')) == false) {
                refreshResponseTimePercentilesOverTime(true);
            }
            document.location.href="#responseTimePercentilesOverTime";
        } else if (elem.id == "bodyResponseTimeDistribution") {
            if (isGraph($(elem).find('.flot-chart-content')) == false) {
                refreshResponseTimeDistribution();
            }
            document.location.href="#responseTimeDistribution" ;
        } else if (elem.id == "bodySyntheticResponseTimeDistribution") {
            if (isGraph($(elem).find('.flot-chart-content')) == false) {
                refreshSyntheticResponseTimeDistribution();
            }
            document.location.href="#syntheticResponseTimeDistribution" ;
        } else if (elem.id == "bodyActiveThreadsOverTime") {
            if (isGraph($(elem).find('.flot-chart-content')) == false) {
                refreshActiveThreadsOverTime(true);
            }
            document.location.href="#activeThreadsOverTime";
        } else if (elem.id == "bodyTimeVsThreads") {
            if (isGraph($(elem).find('.flot-chart-content')) == false) {
                refreshTimeVsThreads();
            }
            document.location.href="#timeVsThreads" ;
        } else if (elem.id == "bodyCodesPerSecond") {
            if (isGraph($(elem).find('.flot-chart-content')) == false) {
                refreshCodesPerSecond(true);
            }
            document.location.href="#codesPerSecond";
        } else if (elem.id == "bodyTransactionsPerSecond") {
            if (isGraph($(elem).find('.flot-chart-content')) == false) {
                refreshTransactionsPerSecond(true);
            }
            document.location.href="#transactionsPerSecond";
        } else if (elem.id == "bodyTotalTPS") {
            if (isGraph($(elem).find('.flot-chart-content')) == false) {
                refreshTotalTPS(true);
            }
            document.location.href="#totalTPS";
        } else if (elem.id == "bodyResponseTimeVsRequest") {
            if (isGraph($(elem).find('.flot-chart-content')) == false) {
                refreshResponseTimeVsRequest();
            }
            document.location.href="#responseTimeVsRequest";
        } else if (elem.id == "bodyLatenciesVsRequest") {
            if (isGraph($(elem).find('.flot-chart-content')) == false) {
                refreshLatenciesVsRequest();
            }
            document.location.href="#latencyVsRequest";
        }
    }
}

/*
 * Activates or deactivates all series of the specified graph (represented by id parameter)
 * depending on checked argument.
 */
function toggleAll(id, checked){
    var placeholder = document.getElementById(id);

    var cases = $(placeholder).find(':checkbox');
    cases.prop('checked', checked);
    $(cases).parent().children().children().toggleClass("legend-disabled", !checked);

    var choiceContainer;
    if ( id == "choicesBytesThroughputOverTime"){
        choiceContainer = $("#choicesBytesThroughputOverTime");
        refreshBytesThroughputOverTime(false);
    } else if(id == "choicesResponseTimesOverTime"){
        choiceContainer = $("#choicesResponseTimesOverTime");
        refreshResponseTimeOverTime(false);
    }else if(id == "choicesResponseCustomGraph"){
        choiceContainer = $("#choicesResponseCustomGraph");
        refreshCustomGraph(false);
    } else if ( id == "choicesLatenciesOverTime"){
        choiceContainer = $("#choicesLatenciesOverTime");
        refreshLatenciesOverTime(false);
    } else if ( id == "choicesConnectTimeOverTime"){
        choiceContainer = $("#choicesConnectTimeOverTime");
        refreshConnectTimeOverTime(false);
    } else if ( id == "choicesResponseTimePercentilesOverTime"){
        choiceContainer = $("#choicesResponseTimePercentilesOverTime");
        refreshResponseTimePercentilesOverTime(false);
    } else if ( id == "choicesResponseTimePercentiles"){
        choiceContainer = $("#choicesResponseTimePercentiles");
        refreshResponseTimePercentiles();
    } else if(id == "choicesActiveThreadsOverTime"){
        choiceContainer = $("#choicesActiveThreadsOverTime");
        refreshActiveThreadsOverTime(false);
    } else if ( id == "choicesTimeVsThreads"){
        choiceContainer = $("#choicesTimeVsThreads");
        refreshTimeVsThreads();
    } else if ( id == "choicesSyntheticResponseTimeDistribution"){
        choiceContainer = $("#choicesSyntheticResponseTimeDistribution");
        refreshSyntheticResponseTimeDistribution();
    } else if ( id == "choicesResponseTimeDistribution"){
        choiceContainer = $("#choicesResponseTimeDistribution");
        refreshResponseTimeDistribution();
    } else if ( id == "choicesHitsPerSecond"){
        choiceContainer = $("#choicesHitsPerSecond");
        refreshHitsPerSecond(false);
    } else if(id == "choicesCodesPerSecond"){
        choiceContainer = $("#choicesCodesPerSecond");
        refreshCodesPerSecond(false);
    } else if ( id == "choicesTransactionsPerSecond"){
        choiceContainer = $("#choicesTransactionsPerSecond");
        refreshTransactionsPerSecond(false);
    } else if ( id == "choicesTotalTPS"){
        choiceContainer = $("#choicesTotalTPS");
        refreshTotalTPS(false);
    } else if ( id == "choicesResponseTimeVsRequest"){
        choiceContainer = $("#choicesResponseTimeVsRequest");
        refreshResponseTimeVsRequest();
    } else if ( id == "choicesLatencyVsRequest"){
        choiceContainer = $("#choicesLatencyVsRequest");
        refreshLatenciesVsRequest();
    }
    var color = checked ? "black" : "#818181";
    if(choiceContainer != null) {
        choiceContainer.find("label").each(function(){
            this.style.color = color;
        });
    }
}

