package wordpress

const (
	API_PATH     = "wp-json/wp/v2"
	OverviewID   = 146
	OverviewType = CONTENT_BLOCK
)

type Type string

const (
	CONTENT_BLOCK Type = "content_block"
	PAGE          Type = "page"
)

type Post struct {
	ID      int     `json:"id"`
	Type    Type    `json:"type"`
	Title   Title   `json:"title"`
	Content Content `json:"content"`
}

type Title struct {
	Rendered string `json:"rendered"`
}

type Content struct {
	Rendered  string `json:"rendered"`
	Protected bool   `json:"protected"`
}
