CREATE TABLE pdf_chunks (
    id BIGSERIAL PRIMARY KEY,
    pdf_document_id BIGINT NOT NULL,
    chunk_text TEXT NOT NULL,
    chunk_order INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pdf_document FOREIGN KEY (pdf_document_id) REFERENCES pdf_documents(id) ON DELETE CASCADE
);

CREATE INDEX idx_pdf_chunks_document ON pdf_chunks(pdf_document_id);
