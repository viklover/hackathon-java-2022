let xhr = new XMLHttpRequest();

let address = 'http://10.3.4.224:8080/'

xhr.open('POST', address, true);

let request_types = {
    request: "get_types_documents"
}

xhr.send(JSON.stringify(request_types));

xhr.onload = function() {
    let responseObj = xhr.response;
    console.log(JSON.parse(responseObj))

    responseObj = JSON.parse(responseObj)

    for (let i = 0; i < responseObj.types.length; i++) {

        let type_config = responseObj.types[i]

        let type_name = type_config.name;

        let content = document.querySelector(".content");
    
        let element = document.createElement('div');
        element.classList.add("file-type");
        element.innerHTML = `
        <div class="file-type-name">${type_name}</div>
        <div class="file-type-bn">
            <svg width="16" height="14" viewBox="0 0 16 14" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M8 14L0.20577 0.5L15.7942 0.500002L8 14Z"/>
            </svg>
        </div>
        <div class="white-block"></div>
        `
        
        content.append(element);

        let elementTable = document.createElement('table');
        elementTable.innerHTML = `
        <tr class="sort">
            <td class="sort-cat sort-check-status">
                <div class="status-check-true"></div>
            </td>
            <td class="sort-cat sort-filenames"><div class="sort-content">
                <div class="text">Имя</div>
                <div class="sort-bn">
                    <svg width="16" height="14" viewBox="0 0 16 14" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M8 14L0.20577 0.5L15.7942 0.500002L8 14Z"/>
                    </svg>
                </div>
            </div></td>
            <td class="sort-cat sort-author"><div class="sort-content">
                <div class="text">Автор</div>
                <div class="sort-bn">
                    <svg width="16" height="14" viewBox="0 0 16 14" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M8 14L0.20577 0.5L15.7942 0.500002L8 14Z"/>
                    </svg>
                </div>
            </div></td>
            <td class="sort-cat sort-editdate"><div class="sort-content">
                <div class="text">Дата изменения</div>
                <div class="sort-bn">
                    <svg width="16" height="14" viewBox="0 0 16 14" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M8 14L0.20577 0.5L15.7942 0.500002L8 14Z"/>
                    </svg>
                </div>
            </div></td>
            <td class="sort-cat"></td>
        </tr>
        `

        content.append(elementTable);
        


        let xhr2 = new XMLHttpRequest();

        xhr2.open('POST', address, true);

        let request_docs = {
            request: "get_documents_by_type", 
            type: type_config.id
        }

        xhr2.send(JSON.stringify(request_docs));

        xhr2.onload = function() {
            
            let responseObjdocs = xhr2.response;
            console.log(JSON.parse(responseObjdocs))

            responseObjdocs = JSON.parse(responseObjdocs)

            for (let doc of responseObjdocs.documents) {

                let doc_name = doc.name
                let doc_author = doc.author
                let doc_was_updated = doc.was_updated
                let doc_checked = doc.checked
                let doc_id = doc.id
                var date = new Date(doc_was_updated);
                doc_was_updated = ('0' + date.getDate()).slice(-2) + '.' + ('0' + (date.getMonth() + 1)).slice(-2) + '.' + date.getFullYear() + ' ' + ('0' + date.getHours()).slice(-2) + ':' + ('0' + date.getMinutes()).slice(-2) + ':' + ('0' + date.getSeconds()).slice(-2);

                let table = document.querySelectorAll("table")[i];
            
                let elementTr = document.createElement('tr');
                elementTr.innerHTML = `
                <td class="sort-cat sort-check-status">
                    <div class="status-check-${doc_checked}"></div>
                </td>
                <td>
                   <div class="name_file">
                       ${doc_name}
                   </div>
                </td>
                <td>
                    <div class="name_author">
                        ${doc_author}
                    </div>
                </td>
                <td>
                    <div class="data">
                        ${doc_was_updated}
                    </div>
                </td>
                <td>
                    <div class="input__wrapper">

                        <a class="bn31 open-file" href="./edit_page" data-id="${doc_id}">
                            <span class="bn31span">Открыть документ</span>
                        </a>
                    </div>
                </td>
                `
                
                table.append(elementTr);
                page_init()
            }
        }

    }

    page_init()

};