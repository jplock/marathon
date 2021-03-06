/** @jsx React.DOM */

define([
  "React"
], function(React) {
  return React.createClass({
    onClick: function() {
      this.props.onClick(this.props.model);
    },

    render: function() {
      var model = this.props.model;

      var instancesClassSet = React.addons.classSet({
        "badge badge-circlet": true,
        "badge-circlet-default": !model.allInstancesBooted()
      });

      return (
        // Set `title` on cells that potentially overflow so hovering on the
        // cells will reveal their full contents.
        <tr onClick={this.onClick}>
          <td className="overflow-ellipsis" title={model.get("id")}>
            {model.get("id")}
          </td>
          <td className="overflow-ellipsis" title={model.get("cmd")}>
            {model.get("cmd")}
          </td>
          <td className="text-right">{model.get("mem")}</td>
          <td className="text-right">{model.get("cpus")}</td>
          <td className="text-right">
            <span className={instancesClassSet}>
              {model.allInstancesBooted() ? null : <span className="health-dot health-dot-warning"></span>}
              {model.formatTasksRunning()} / {model.get("instances")}
            </span>
          </td>
        </tr>
      );
    }
  });
});
