$(function() { 
  
  // Create an jstree instance
  $('#jstree_demo').jstree({ // config object start
    
    "core": {                    // core config object
      "mulitple": false,         // disallow multiple selection  
      "animation": 100,          // 200ms is default value
      "check_callback" : true,   // this make contextmenu plugin to work
      "themes": {
        "variant": "medium",
        "dots": false
      },

      "data": [
        // The required JSON format for populating a tree
        { "text": "Root",
          "state": { 
            "opened": true
          },
         "type": "demo",
         "children": [ // "children" key is an array of objects
            { "text": "Child node 1",
              "li_attr": { 
                "class": "li-style"
             },
             "a_attr": {
               "class": "a-style"
             }
            },
            { "text": "Child node 2",
             "state": {
               "opened": true
             },
             "children": [
               { "text": "Grandchild 1",
                 "state": {
                   // "disabled": true,
                   "selected": true
                }
               },
               { "text": "Grandchild 2",
                 "state": {
                   "disabled": false
                },
                "children": [ "Grandson 1", "Grandson 2" ]
               },
               { "text": "Grandchild 3"
               //  "icon": "glyphicon glyphicon-upload"
               },
             ]
            },
          {"text": "Child node 3",
           // if all we need is node with the given text, it can be written like this
           "children": [ "Grandchild 1", "Grandchild 1" ]
          },
         ]} // root node end, end of JSON
      ] // data core options end
      
    }, // core end
    
    // Types plugin
    "types" : {
      "default" : {
     //   "icon" : "glyphicon glyphicon-flash"
      },
      "demo" : {
     //   "icon" : "glyphicon glyphicon-th-large"
      }
    },
    
    // config object for Checkbox plugin (declared below at plugins options)
    "checkbox": { 
      "keep_selected_style": false,  // default: false
      "three_state": true,           // default: true
      "whole_node": true             // default: true
    },
    
    "conditionalselect" : function (node, event) {
      return false;
    },
    
    // injecting plugins
    "plugins" : [
          "checkbox",
          "contextmenu",
          // "dnd",
          // "massload",
          // "search",
          // "sort",
          // "state",
          "types",
          // Unique plugin has no options, it just prevents renaming and moving nodes
          // to a parent, which already contains a node with the same name.
          "unique",
          // "wholerow",
          // "conditionalselect",
          "changed"
    ]
  }); // config object end
  
   // AJAX loading JSON Example:
   $('#jstree_ajax_demo').jstree({
    'core': {
      'data': {
        "url" : "https://codepen.io/stefanradivojevic/pen/dWLZOb.js",
        "dataType" : "json" // needed only if you do not supply JSON headers
      }
    },
    // Types plugin
    "types" : {
      "default" : {
      //  "icon" : "glyphicon glyphicon-record"
      }
    },
     "plugins" : [ "types", "unique" ]
  });
  
  // Listen for events - example
  $('#jstree_demo_div').on("changed.jstree", function (e, data) {
    // changed.jstree is a event
    // console.log(data.selected);
    console.log('ds: ' + data.changed.deselected);
  });
  
});