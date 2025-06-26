// start pop_tab 
function pop_tab_open_page(pageName, elmnt, color) {
    var i, pop_tab_nav_content, pop_tab_nav_links;

    var header = document.getElementById("pop_tab_content_wrap");
    var btns = header.getElementsByClassName("pop_tab_btn");

    for (var i = 0; i < btns.length; i++) {
        btns[i].addEventListener("click", function() {
            var current = document.getElementsByClassName("pop_tab_action");
            current[0].className = current[0].className.replace(" pop_tab_action", "");
            this.className += " pop_tab_action";
        });
    }

    pop_tab_nav_content = document.getElementsByClassName("pop_tab_nav_content");

    for (i = 0; i < pop_tab_nav_content.length; i++) {
        pop_tab_nav_content[i].style.display = "none";
    }

    pop_tab_nav_links = document.getElementsByClassName("pop_tab_nav_link");
    for (i = 0; i < pop_tab_nav_links.length; i++) {
        pop_tab_nav_links[i].style.backgroundColor = "";
    }
    document.getElementById(pageName).style.display = "block";
    elmnt.style.backgroundColor = color;
    elmnt.style.fontColor = color;
}

$(function() { 
    // left_pannel
    var posY;

    $(".bnt_open").on("click", function(e) {
		openSliderMenu()
    });
    $(".bnt-close").on("click", function() {
		closeSliderMenu();
    });
    //document.getElementById("pop_tab_default_open").click();

    // arrcordion 
    //$("#accordion .accordion_bar").accordion();
});
let posY;
function openSliderMenu() {
    posY = $(window).scrollTop();

    $("html, body").addClass("not-scroll");
    $(".left-slidebar").css("display", "block");
    $(".left_overlay").css("display", "block");
    $(".left-slide-pannel-nav").css("display", "block");
    $(".body_content_wrap").css("top", -posY);
}
function closeSliderMenu() {
    $("html, body").removeClass("not-scroll");
    $(".left-slidebar").css("display", "none");
    $(".left_overlay").css("display", "none");
    $(".left-slide-pannel-nav").css("display", "none");
    posY = $(window).scrollTop(posY);
}

