<%
  String[][] searchTabs = {{"everything","everything"},{"films",Types.FILM},{"music",Types.MUSIC},{"disks",Types.ISO},{"pictures",Types.PICTURE},{"advanced","advanced"}};
%>
<div id="searchTabsContainer">
    <table cellspacing=0 id="searchTabs">
        <tr>
            <td style="width:50%">&#160;</td>
            <%
                for (String[] tab: searchTabs)
                    {           
                        if (tab[1].equals(params.type))
                        {
            %><td class="tab selected"><span><%=tab[0]%></span></td><%
                }
                        else
                        {
            %><td class="tab"><a href="changeSearchType" onclick="changeSearchType('<%= tab[1] %>'); return false;"><%=tab[0]%></a></td><%
                }
                        if (searchTabs[searchTabs.length-1]!=tab)
                        {
                            int padding = 2;
                            if (searchTabs[searchTabs.length-2]==tab) padding = 50; // spacer before "Advanced" tab
            %><td class="spacer" style="padding-left:<%=padding%>px;">&#160;</td><%
                }
                    }
            %>
            <td style="width:50%">&#160;</td>
        </tr>
    </table>
</div>

<div id="searchFormContainer">
    <!--div style="position:absolute; left:0px; top:0px; width:100px; height:100%; background-color:#FF7B00; font-size:1px;"></div-->
    <form id="searchForm" action="search.jsp" method="get">
        <input type="hidden" name="type" value="<%=params.type%>" />
        <%
            if (params.type.equals("advanced"))
                {
        %>
            <table class="fieldset" width="100%" style="font-size:16px; font-weight:bold;">
                <tr>
                    <td>
                        path<br/><input type="text" id="dir"  name="dir"  value="<%= params.dir  %>" size="20" />
                    </td>
                    <td>
                        filename<br/><input type="text" id="file" name="file" value="<%= params.file %>" size="20" />
                    </td>
                    <td>
                        extension<br/><input  type="text" id="ext"  name="ext"  value="<%= params.ext  %>" size="10" />
                    </td>
                </tr>
                <tr>
                    <td>
                        date<span style="vertical-align: sub; font-size: 12px;">yyyy-mm-dd</span><br/>
                        <input type="text" id="fromDate" name="fromDate" value="<%= SearchParams.getStringValue(request, "fromDate") %>" size="11" style="font-size:16px; font-weight:bold"/>&nbsp;-
                        <input type="text" id="toDate" name="toDate" value="<%= SearchParams.getStringValue(request, "toDate") %>" size="11" style="font-size:16px; font-weight:bold"/>                     
                    </td>
                    <td>
                        size<span style="vertical-align: sub; font-size: 12px;">Mb</span><br/>
                        <input type="text" id="minSize" name="minSize" value="<%= SearchParams.getStringValue(request, "minSize") %>" size="6" style="font-size:16px; font-weight:bold"/>&nbsp;-
                        <input type="text" id="maxSize" name="maxSize" value="<%= SearchParams.getStringValue(request, "maxSize") %>" size="6" style="font-size:16px; font-weight:bold"/>                       
                    </td>
                    <td>
                        <input type="submit" value="search"/>
                    </td>                       
                </tr>               
                <!--tr>
                    <td>
                        Hint<span style="color:white; font-weight:normal"> use "!" to negate search terms</span>                
                    </td>
                </tr-->                     
            </table>                        
        <%
                                    }
                                                                else
                                                                {
                                %>
            <input id="query" type="text" name="query" value="<%=params.query%>" style="width:480px;" />
            <input id="submit" type="submit" value="search"/>
        <%
                            }
                        %>
    </form>
</div>