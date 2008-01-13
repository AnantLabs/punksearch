<div id="searchResultsContainer">
    <%
        Map parameterMap = request.getParameterMap();
        if (!parameterMap.isEmpty() && !(parameterMap.size() == 1 && parameterMap.get("type") != null))
        {   
            SearchAction searchAction  = new SearchAction(params);
            List<ItemGroup> searchResults = searchAction.doSearchGroupped();
            if(!searchResults.isEmpty())
            {
    %>
                <table id="pager" cellspacing="0" cellpadding="0" align="center">
                    <tr>
                        <td style="font-size: 12px; text-align: left; padding-left: 2px;">
                        <%
                          int items = searchAction.getOverallCount();
                          int pages = SearchPager.getPageCount(searchAction.getOverallCount());
                          long searchTime = searchAction.getSearchTime();
                          long presentationTime = searchAction.getPresentationTime();
                        %>
                            <span style="font-size: 18px;"><%=items%></span> items (<%=pages%> pages) in <span title="<%=searchTime/1000.0 + " search + " + presentationTime/1000.0 + " presentation"%>"><%= (searchTime + presentationTime)/1000.0 %></span> secs
                        </td>
                        <td style="text-align: right; vertical-align: bottom;"><%=SearchPager.makePagesRow(request, searchAction.getOverallCount())%></td>
                    </tr>
                </table>
                <table id="results" cellspacing="0" align="center">
                <%
                    int counter1 = 0;
                                    int counter2 = 0;
                                    for (ItemGroup group : searchResults)
                                    {
                                        if (counter1 < params.first) {
                                            counter1++;
                                            continue;
                                        }
                                        if (counter2 > params.last - params.first) {
                                            break;
                                        }
                                        SearchResult file = new SearchResult(group.getItems().get(0));
                                        boolean online = CachedOnlineChecker.isOnline(file.host);
                                        counter2++;
                %>
                    <tr>
                        <td style="width: 16px; padding-right: 2px; vertical-align: top; padding-top: 4px;">
                            <img src="images/<%= (file.ext.length() != 0)? "stock_new-16.gif" : "stock_folder-16.gif" %>"/>
                            <div style='font-size: 4px; margin: 1px; background-color: <%= (online) ?   "#00FF00;" : "#FF0000;" %>;'>&nbsp;&nbsp;</div>
                        </td>
                        <td style="padding-left: 2px;">
                            <span style="font-size: 12pt;" class="name"><%=file.name%></span>
                            <span class="more"><%=(group.getItems().size() > 1)? "( <a href=\"#\" onClick=\"toggle('subGroup"+counter2+"');\">" + (group.getItems().size() - 1) + " more</a> )": ""%></span>
                            <%=(showScores)? "(" + file.score + ")": ""%><br/>
                            <span style="font-size: 8pt;" class="path"><%=file.host + "/" + file.path%></span>
                            <br/>
                            <div class="othersInGroup" id="subGroup<%= counter2 %>" style="display:none;">
                            <table>
                            <%
                                for (int i = 1; i < group.getItems().size(); i++ ) {
                                                                           SearchResult subFile = new SearchResult(group.getItems().get(i));
                                                                           boolean subOnline = CachedOnlineChecker.isOnline(subFile.host);
                            %>
                                    <tr>
                                        <td>
                                            <img src="images/<%= (subFile.ext.length() != 0)? "stock_new-16.gif" : "stock_folder-16.gif" %>"/>
                                            <div style='font-size: 4px; margin: 1px; background-color: <%= (subOnline) ?   "#00FF00;" : "#FF0000;" %>;'>&nbsp;&nbsp;</div>
                                        </td>
                                        <td>
                                           <span class="name"><%= subFile.name %></span><br/>
                                           <span class="path"><%= subFile.host + "/" + subFile.path %></span>
                                        </td>
                                    </tr>
                                   <%
                              }
                            %>
                            </table>
                            </div>
                        </td>
                        <td style="text-align: right;"><%= file.date %></td>
                        <td style="text-align: right;"><%= file.size %> Mb</td>
                    </tr>
                    <%
                    }
                %>
                </table>
            <%  
            }
            else
            {
                %><div class="infoMessage">search yields no results</div><% 
            }
        }
    %>
</div>
