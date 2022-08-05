
# Search code using GitHub Search API

The [Search API](https://docs.github.com/en/rest/search#search-code) lets you to search for specific items on GitHub. This method returns up to 100 results per page.

This script is developed using Python. You can search for a phrase using this script and it will return the results in the following format to a csv file.

``` repository_owner/repository_name,file_path ```




## How to run?

First, generate a personal access token from your profile on GitHub's website: 

Settings -> Developer Settings -> Personal Access Token -> Generate New Token



### Environment Variables

To run this project, you will need to set the PAT as an environment variable.

`export TOKEN=<YOUR_PERSONAL_ACCESS_TOKEN>`

###

Replace your search phrase in the query section. This query will return 50 results per page.

```javascript
def get_data(num):
  try:
    url = "https://api.github.com/search/code?q=<SEARCH_PHRASE>&page="+str(num)+"&per_page=50"
```

Under the main function, change the range accordingly with respect to the pages available in the GitHub search function.

```
def main():
  for i in range(1,6):
```

## Usage/Examples

Run the Python script:

``` python3 git-code-search.py ```


