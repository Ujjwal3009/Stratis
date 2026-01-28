-- Create pdf_documents table
CREATE TABLE pdf_documents (
    id BIGSERIAL PRIMARY KEY,
    filename VARCHAR(255) NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT NOT NULL,
    document_type VARCHAR(50) NOT NULL,
    upload_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    uploaded_by BIGINT REFERENCES users(id),
    status VARCHAR(50) DEFAULT 'UPLOADED',
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better query performance
CREATE INDEX idx_pdf_document_type ON pdf_documents(document_type);
CREATE INDEX idx_pdf_uploaded_by ON pdf_documents(uploaded_by);
CREATE INDEX idx_pdf_status ON pdf_documents(status);
CREATE INDEX idx_pdf_upload_date ON pdf_documents(upload_date);

-- Add comments
COMMENT ON TABLE pdf_documents IS 'Stores metadata for uploaded PDF documents (PYQs, books, current affairs)';
COMMENT ON COLUMN pdf_documents.document_type IS 'Type of document: PYQ, BOOK, CURRENT_AFFAIRS';
COMMENT ON COLUMN pdf_documents.status IS 'Processing status: UPLOADED, PROCESSING, PROCESSED, FAILED';
