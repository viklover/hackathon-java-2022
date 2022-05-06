let xhr3 = new XMLHttpRequest();

xhr3.open('POST', 'http://10.3.4.224:8080/', true);

let request_doc_data = {
    request: "get_document_by_id", id: getCookie("id_doc")
}

xhr3.send(JSON.stringify(request_doc_data));

xhr3.onload = function() {
    let type_name = document.querySelector(".type_name")
    let name = document.querySelector(".name")
    let responseDoc = xhr3.response;
    console.log(JSON.parse(responseDoc))

    responseDoc = JSON.parse(responseDoc)

    let p = document.createElement('p');
        p.innerHTML = `${responseDoc.document.type_name}`

    type_name.append(p)

    let h1 = document.createElement('h1');
        h1.innerHTML = `${responseDoc.document.name}`

    name.append(h1)

    document.getElementById("check").checked = responseDoc.document.checked

    let table_content = document.querySelector("tbody")

    for (let key of Object.keys(responseDoc.document.fields)) {
        let doc_data = responseDoc.document.fields[key]
        let tr = document.createElement('tr')
            tr.innerHTML = `
            <tr>
                <td><div class="one" data-id="${key}" >${doc_data[0]}</div></td>
                <td><input type="text" name="field" style="text-align:center;" value="${(doc_data[1] != null ? doc_data[1] : "")}"></td>
            </tr>
            `
        table_content.append(tr)
    }



    let save_btn = document.querySelector(".savebtn");
    
    save_btn.onclick = function() {
        
        if (document.getElementById("name").value != "") {

            let xhr4 = new XMLHttpRequest();
        
            xhr4.open('POST', 'http://10.3.4.224:8080/', true);
        
            let table_content2 = document.querySelector("tbody")
            let data = {};
        
            let fields = document.querySelectorAll(".one")
            let fields_content = table_content2.querySelectorAll("input")
            for (let i = 0; i < fields.length; i++) {
                data[Object.keys(responseDoc.document.fields)[i]] = fields_content[i].value
            } 
        
            let set_document_fields = {
                request: "set_document_fields", 
                id: getCookie("id_doc"),
                author: document.getElementById("name").value,
                checked: document.getElementById("check").checked,
                fields: data
            }
        
            xhr4.send(JSON.stringify(set_document_fields));
            location.reload();
        } else {
            alert("Введите имя")
        }
    }
}



let xhr5 = new XMLHttpRequest();

xhr5.open('POST', 'http://10.3.4.224:8080/', true);

let get_document_history = {
    request: "get_document_history", id: getCookie("id_doc")
}

xhr5.send(JSON.stringify(get_document_history));

xhr5.onload = function() {
    let responseHis = xhr5.response;
    console.log(JSON.parse(responseHis))

    let verGrid = document.querySelector(".versions");

    for (let ver of JSON.parse(responseHis).versions) {
        let numVer = ver[0];

        let hisItem = document.createElement('div')
            hisItem.innerHTML = `
            <div class="verBtn"> 
                <a class="verBtnContent" href="#" data-id="${numVer}">Версия ${numVer}</a>
            </div>
            `
        verGrid.append(hisItem)
    }
    let ver_btn = document.querySelectorAll(".verBtnContent")
    for (let vbtn of ver_btn) {
        vbtn.onclick = function() {
            let table_content2 = document.querySelector("tbody")
    
            let fields = document.querySelectorAll(".one")
            let fields_content = table_content2.querySelectorAll("input")
            for (let i = 0; i < fields.length; i++) {
                fields_content[i].value = JSON.parse(responseHis).versions[vbtn.dataset.id-1][i+2]
            }
            document.getElementById("name").value = JSON.parse(responseHis).versions[vbtn.dataset.id-1][1]
            if (vbtn.dataset.id-1 < JSON.parse(responseHis).versions.length-1) {
                let inputs = document.querySelectorAll("input")
                for (let inp of inputs) {
                    inp.setAttribute("readonly", true)
                }
                let save_btn = document.querySelector(".savebtn");
                save_btn.setAttribute("style","display: none;")
            } else {
                let inputs = document.querySelectorAll("input")
                for (let inp of inputs) {
                    inp.removeAttribute("readonly")
                }
                let save_btn = document.querySelector(".savebtn");
                save_btn.removeAttribute("style")
            }
        }
    }
}