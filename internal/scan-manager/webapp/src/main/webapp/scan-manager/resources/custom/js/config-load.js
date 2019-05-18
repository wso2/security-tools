/**
 * Files can be uploaded by uploading the file through the form or by providing a URL to download the
 * file. This function will display the appropriate input element (with the type 'file' or 'text') according to
 * the selected check box (by File or by URL).
 *
 * @param elementId element id of the file element that needs to be changed in to type 'file' or 'text'
 */
function changeFileUploadMethod(elementId) {
    let defaultCheckBox = document.getElementById(elementId + 'DefaultCheckbox');

    if (defaultCheckBox == null || !defaultCheckBox.checked) {
        let fileElement = document.getElementById(elementId);   // input element with type file.
        let urlElement = document.getElementById(elementId + '@byURL'); // input element with type text.
        let maxFileSizeLabel = document.getElementById(elementId + 'FileSizeLabel');

        if (document.getElementById(elementId + 'byFileCheck').checked) {

            // if the user has selected the "by File" radio button.
            // display the input element with the type file.
            if (fileElement != null) {
                fileElement.style.display = 'block';
                fileElement.required = true;
                fileElement.removeAttribute("disabled");

                maxFileSizeLabel.style.display = 'block';
                maxFileSizeLabel.removeAttribute("disabled");
            }

            // hide the input element with the type text.
            if (urlElement != null) {
                urlElement.style.display = 'none';
                urlElement.required = false;
                urlElement.setAttribute("disabled", "disabled");
            }
        } else if (document.getElementById(elementId + 'byURLCheck').checked) {

            // if the user has selected the "by URL" radio button.
            // display the input element with the type text.
            if (urlElement != null) {
                urlElement.style.display = 'block';
                urlElement.required = true;
                urlElement.removeAttribute("disabled");
            }

            // hide the input element with the type file.
            if (fileElement != null) {
                fileElement.setAttribute("disabled", "disabled");
                fileElement.style.display = 'none';
                fileElement.required = false;

                maxFileSizeLabel.style.display = 'none';
                maxFileSizeLabel.setAttribute("disabled", "disabled");
            }
        }
    }
}

/**
 * This function is used to disable the element when 'use default' checkbox is checked.
 *
 * @param elementId element id of the element to be disabled
 */
function useDefault(elementId) {

    // the element to be disabled.
    let currentElement = document.getElementById(elementId);

    // if the input element is a file upload by url.
    let currentFileURLElement = document.getElementById(elementId + '@byURL');

    // by File or by URL checkbox elements.
    let currentFileCheckbox = document.getElementById(elementId + 'byFileCheck');
    let currentURLCheckbox = document.getElementById(elementId + 'byURLCheck');

    if (document.getElementById(elementId + 'DefaultCheckbox').checked) {

        // if the "use default" is checked, disable input elements from submitting through the form.
        if (currentElement != null) {
            currentElement.setAttribute("disabled", "disabled");
        }
        if (currentFileURLElement != null) {
            currentFileURLElement.setAttribute("disabled", "disabled");
        }
        if (currentFileCheckbox != null) {
            currentFileCheckbox.setAttribute("disabled", "disabled");
        }
        if (currentURLCheckbox != null) {
            currentURLCheckbox.setAttribute("disabled", "disabled");
        }

    } else {

        // if the "use default" not checked, enable input elements from submitting through the form.
        if (currentElement != null) {
            currentElement.removeAttribute("disabled");
        }
        if (currentFileURLElement != null) {
            currentFileURLElement.removeAttribute("disabled");
        }
        if (currentFileCheckbox != null) {
            currentFileCheckbox.removeAttribute("disabled");
        }
        if (currentURLCheckbox != null) {
            currentURLCheckbox.removeAttribute("disabled");
        }
    }
}