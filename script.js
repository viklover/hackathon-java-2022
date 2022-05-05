function page_init() {

    let switch_list = document.querySelectorAll(".file-type");
    let switch_icon = document.querySelectorAll(".file-type-bn");
    let table_list = document.querySelectorAll("table");

    function switch_table(btn, i) {

        btn.onclick = function() {
            switch_icon[i].classList.toggle("rotated");
            table_list[i].classList.toggle("visible");
        }
    }

    for (let i = 0; i < switch_list.length; i++) {
        
        switch_table(switch_list[i], i); 
    }
    
}