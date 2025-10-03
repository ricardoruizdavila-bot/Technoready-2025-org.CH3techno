The main goal of this project is to **automate the integration of information from the institution’s Top 3 researchers**.  
By leveraging the Google Scholar API and a structured Java application, the project eliminates manual processes and ensures that research data is consistently updated in the university’s research database.

- **API Review & Mapping**: Analysis of Google Scholar API documentation and mapping of JSON data to the database structure.  
- **Version Control**: GitHub repository for version management, documentation, and collaboration.  
- **Data Extraction**: Java code to fetch researcher information from Google Scholar and store it in memory.  
- **Database Integration**: Automatic population of the institutional research database with validated information.  
- **Progress Monitoring**: Periodic team reviews and documentation updates in GitHub.

This project addresses the challenge of **time-consuming and error-prone manual data collection** for researcher evaluation.  
With automation:  
- Researchers’ profiles and publications are kept up to date.  
- Administrative effort is reduced significantly.  
- Data reliability and transparency are improved for academic decision-making.  

Scholar MVC (Java 17)

Java MVC app that performs GET requests to Google Scholar via SerpAPI and prints results to the console.
Primary mode: Papers – search scholarly articles by keyword (e.g., “biology”).
Optional (Author in Google Scholar): by name, resolving the author_id internally and then querying the profile (no id required from the user).
Note: Google Scholar does not provide a public “search author by name” API. The optional author flow resolves the id by searching Google and then calls google_scholar_author (SerpAPI).

Requirements

Java 17
Maven 3.8+
SerpAPI account (API Key) — https://serpapi.com/

Project estructure
scholar-mvc/
├─ pom.xml
├─ src/
│  ├─ main/java/org/CH3techno/scholar/
│  │  ├─ App.java                     # entry point
│  │  ├─ controller/
│  │  │  ├─ PaperController.java      # C (papers)
│  │  │  └─ AuthorController.java     # C (author, optional)
│  │  ├─ service/
│  │  │  └─ ScholarApiClient.java     # HTTP client (HttpClient5 + Jackson)
│  │  ├─ view/
│  │  │  ├─ ConsolePaperView.java     # V (papers)
│  │  │  └─ ConsoleAuthorView.java    # V (author)
│  │  └─ model/
│  │     ├─ Paper.java
│  │     ├─ PaperSearchResponse.java
│  │     ├─ Author.java               # optional
│  │     ├─ AuthorSearchResponse.java # optional
│  │     └─ Publication.java          # optional (author’s articles)
│  └─ test/java/org/CH3techno/scholar/
│     ├─ PaperControllerTest.java
│     ├─ PaperMappingTest.java
│     └─ AuthorControllerTest.java    # optional

Google Scholar – PAPERS (primary)
export SCHOLAR_MODE="papers"
export SCHOLAR_API_BASE_URL="https://serpapi.com/search.json"
export SCHOLAR_API_KEY="REPLACE_WITH_YOUR_API_KEY"

export SCHOLAR_QUERY_PARAM="q"
export SCHOLAR_KEY_PARAM="api_key"
export SCHOLAR_KEY_HEADER=""
export SCHOLAR_EXTRA_QUERY="engine=google_scholar&hl=es"

Google Scholar – AUTHOR by name (optional)

export SCHOLAR_MODE="authors"
export SCHOLAR_API_BASE_URL="https://serpapi.com/search.json"
export SCHOLAR_API_KEY="REPLACE_WITH_YOUR_API_KEY"

# If you still use the generic doRequest path, these help; the service also forces per-method params.
export SCHOLAR_QUERY_PARAM="author_id"
export SCHOLAR_KEY_PARAM="api_key"
export SCHOLAR_KEY_HEADER=""
export SCHOLAR_EXTRA_QUERY="engine=google_scholar_author&hl=es"

# Enable name → author_id resolver
