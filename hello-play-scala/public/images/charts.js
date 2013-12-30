function drawChart(div,sucessos,falhas) {
      // Create the data table.
      var data = new google.visualization.DataTable();
      data.addColumn('string', 'Topping');
      data.addColumn('number', 'Slices');
      data.addRows([
        ['Builds sucess', sucessos],
        ['Builds fails', falhas],
      ]);

      // Set chart options
      var options = {'width':400,
                     'height':300};

      // Instantiate and draw our chart, passing in some options.
      var chart = new google.visualization.PieChart(document.getElementById(div));
      chart.draw(data, options);
}
